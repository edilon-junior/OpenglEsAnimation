package com.example.openglexemple.Animation;

import android.opengl.Matrix;

import com.example.openglexemple.Utils;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.XmlParser.XmlNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {

    public static final String TAG = "ANIMATION";
    private static final float[] CORRECTION = new float[16];

    String id;
    String name;
    float[] times;
    // pose matrix
    public List<XmlNode> childAnimations;
    private final Map<String, float[][]> animationData = new HashMap<>();
    public KeyFrame[] keyFrames;
    float animationTime = 0;
    private float duration;
    private float[] bindShapeMatrix;
    public Animation(XmlNode mainAnimation, String rootJointId){
        Matrix.setIdentityM(CORRECTION, 0);
        Matrix.rotateM(CORRECTION, 0, (float) Math.toRadians(-90), 1.0f, 0.0f, 0.0f);
        this.id= mainAnimation.getAttribute("id");
        this.name = mainAnimation.getAttribute("name");
        this.childAnimations = mainAnimation.getChildren("animation");
        createTimes();
        getAnimationTransforms(rootJointId);
        createKeyFrames();
    }

    public void increaseAnimationTime(float time) {
        animationTime += time;
        if (animationTime > duration) {
            this.animationTime %= duration;
        }
    }

    private void createTimes(){
        XmlNode animation = childAnimations.get(0);
        XmlNode sampler = animation.getChild("sampler");
        String inputSourceStr = sampler.getChildWithAttribute("input", "semantic","INPUT").getAttribute("source");
        XmlNode inputSource = animation.getChildWithAttribute("source", "id", inputSourceStr.substring(1));
        times = Utils.stringToFloatArray(inputSource.getChild("float_array").getData());
        duration = times[times.length-1];
    }

    private void getAnimationTransforms(String rootJointId){
        for(XmlNode animation : childAnimations){
            extractAnimationTransforms(animation, rootJointId );
        }
    }

    private void extractAnimationTransforms(XmlNode animationNode, String rootNodeId){
        String jointId = getJointId(animationNode);
        String transformId = getTransformId(animationNode);
        XmlNode transformData = animationNode.getChildWithAttribute("source", "id", transformId);
        String transformStr = transformData.getChild("float_array").getData();
        String[] transformArr = transformStr.split("\\s+");
        processTransforms(jointId, transformArr, jointId.equals(rootNodeId));
    }
    private String getTransformId(XmlNode animationNode){
        XmlNode node = animationNode.getChild("sampler").getChildWithAttribute("input", "semantic", "OUTPUT");
        return node.getAttribute("source").substring(1);
    }

    private String getJointId(XmlNode animationNode){
        XmlNode channelNode = animationNode.getChild("channel");
        String data = channelNode.getAttribute("target");
        return data.split("/")[0];
    }
    private void processTransforms(String jointId, String[] transforms, Boolean isRootJoint){
        float[][] transformList = new float[times.length][16];

        for(int i=0; i < times.length;i++){
            float[] transform = new float[16];

            for(int j=0; j < 16; j++){
                transform[j] = Float.parseFloat(transforms[i * 16 + j]);
            }

            float[] transpose = new float[16];
            Matrix.transposeM(transpose,0, transform, 0);

            transformList[i] = transpose;

            //multiply here with inv_bind_matrix to optimization
            /*
            if(isRootJoint){
                //because up axis in Blender is different to up axis in game
                Matrix.multiplyMM(transform, 0, CORRECTION, 0,transform,0);
            }
             */
        }

        //remove inverse kinematic animations
        //add animation matrices of joints
        if(!jointId.endsWith("ik")) {
            animationData.put(jointId, transformList);
        }
    }

    /**
     * get joints animation matrices and convert to jointTransform
     */
    private void createKeyFrames(){
        keyFrames = new KeyFrame[times.length];

        for(int i=0; i < keyFrames.length; i++){
            Map<String, JointTransform> jointKeyFrames = new HashMap<>();
            for(Map.Entry<String, float[][]> ad : animationData.entrySet()){
                String jointId = ad.getKey();
                float[] transformMatrix = ad.getValue()[i];

                JointTransform jointAnimationTransform = createTransform(transformMatrix);
                jointKeyFrames.put(jointId, jointAnimationTransform);
            }
            keyFrames[i] = new KeyFrame(times[i], jointKeyFrames);
        }
    }

    /**
     * Creates a joint transform from the data extracted from the collada file.
     *
     * @param t
     *            - the data from the collada file.
     * @return The joint transform.
     */
    private static JointTransform createTransform(float[] t) {
        Vector3f translation = new Vector3f(t[12], t[13], t[14]);
        Quaternion rotation = Quaternion.createQuaternion(t);
        return new JointTransform(translation, rotation);
    }

    public Map<String, float[]> calculateCurrentAnimationPose() {
        KeyFrame[] frames = getPreviousAndNextFrames();
        float progression = calculateProgression(frames[0], frames[1]);
        return interpolatePoses(frames[0], frames[1], progression);
    }

    private KeyFrame[] getPreviousAndNextFrames() {
        KeyFrame previousFrame = keyFrames[0];
        KeyFrame nextFrame = keyFrames[0];
        for (int i = 1; i < keyFrames.length; i++) {
            nextFrame = keyFrames[i];
            if (nextFrame.getTimeStamp() > animationTime) {
                break;
            }
            previousFrame = keyFrames[i];
        }
        return new KeyFrame[] { previousFrame, nextFrame };
    }

    //calculates the proportion in which the current
    // animation time is between the previous frame and the next frame.
    private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
        float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
        float currentTime = animationTime - previousFrame.getTimeStamp();
        return currentTime / totalTime;
    }

    /**
     * Calculates all the local-space joint transforms for the desired current
     * pose by interpolating between the transforms at the previous and next
     * keyframes.
     *
     * @param previousFrame
     *            - the previous keyframe in the animation.
     * @param nextFrame
     *            - the next keyframe in the animation.
     * @param progression
     *            - a number between 0 and 1 indicating how far between the
     *            previous and next keyframes the current animation time is.
     * @return The local-space transforms for all the joints for the desired
     *         current pose. They are returned in a map, indexed by the name of
     *         the joint to which they should be applied.
     */
    private Map<String, float[]> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
        Map<String, float[]> currentPose = new HashMap<>();
        for (String jointId : previousFrame.getJointKeyFrames().keySet()) {
            JointTransform previousTransform = previousFrame.getJointKeyFrames().get(jointId);
            JointTransform nextTransform = nextFrame.getJointKeyFrames().get(jointId);

            float[] poseMatrix = new float[16];
            if(previousTransform != null && nextTransform != null) {
                JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
                poseMatrix = currentTransform.getLocalTransform();
            }else {
                Matrix.setIdentityM(poseMatrix, 0);
            }
            currentPose.put(jointId, poseMatrix);
        }
        return currentPose;
    }

    public void applyPoseToJoints(Map<String, float[]> currentPose, Joint joint, float[] parentTransform, float[][] test, float[] rootIBM) {
        float[] currentLocalTransform = currentPose.get(joint.getId());
        float[] currentTransform = new float[16];

        if(currentLocalTransform == null){
            currentLocalTransform = new float[16];
            Matrix.setIdentityM(currentLocalTransform, 0);
        }

        if(joint.getId().endsWith("ik")){
            currentLocalTransform = new float[16];
            Matrix.setIdentityM(currentLocalTransform,0);
        }
        //multiply joint transform of current pose to parent transforms
        Matrix.multiplyMM(currentTransform, 0,parentTransform, 0, currentLocalTransform, 0);

        for (Joint childJoint : joint.getChildren()) {
            applyPoseToJoints(currentPose, childJoint, currentTransform, test, rootIBM);
        }

        //multiply ibm to current pose transform
        float[] poseXibm = new float[16];
        float[] totalTransform = new float[16];

        //System.out.println(TAG+" currentTransform of "+joint.getId()+" index "+joint.getIndex()+" : "+Arrays.toString(joint.getIBM()));

        Matrix.multiplyMM(poseXibm,0, joint.getIBM(), 0, currentTransform, 0);

        joint.setAnimatedTransform(totalTransform);
        test[joint.getIndex()]= poseXibm;
        //test[joint.getIndex()] = totalTransform;
    }

    public void applyPoseToJoints2(Map<String, float[]> currentPose, Skeleton skeleton, float[][] test) {

        for(Map.Entry<String, Joint> entry : skeleton.getJoints().entrySet()) {
            String jointId = entry.getKey();
            String parentId = entry.getValue().getParentId();

            float[] currentLocalTransform = currentPose.get(jointId);
            float[] currentTransform = new float[16];

            if (currentLocalTransform == null) {
                currentLocalTransform = new float[16];
                Matrix.setIdentityM(currentLocalTransform, 0);
            }

            float[] parentTransform = currentPose.get(parentId);
            if(parentTransform == null){
                parentTransform = new float[16];
                Matrix.setIdentityM(parentTransform, 0);
            }

            //multiply joint transform of current pose to parent transforms
            Matrix.multiplyMM(currentTransform, 0, parentTransform, 0, currentLocalTransform, 0);

            //multiply ibm to current pose transform
            float[] poseXibm = new float[16];
            float[] totalTransform = new float[16];

            //System.out.println(TAG+" currentTransform of "+joint.getId()+" : "+Arrays.toString(currentTransform));

            Matrix.multiplyMM(poseXibm, 0, skeleton.getRootJoint().getIBM(), 0, currentTransform, 0);

            //joint.setAnimatedTransform(totalTransform);
            test[entry.getValue().getIndex()] = poseXibm;
            //test[joint.getIndex()] = totalTransform;
        }
    }


    public void update() {}

    public String getId() {
        return id;
    }
    public String getName(){
        return name;    }

    public float[] getTimes() {
        return times;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(float animationTime) {
        this.animationTime = animationTime;
    }

    public KeyFrame[] getKeyFrames(){
        return keyFrames;
    }

    public void setBindShapeMatrix(float[] bindShapeMatrix) {
        this.bindShapeMatrix = bindShapeMatrix;
    }
    public float[] getBindShapeMatrix(){
        return bindShapeMatrix;
    }
}
