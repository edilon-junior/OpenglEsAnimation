package com.example.openglexemple.Loader.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Animation.Animation;
import com.example.openglexemple.Animation.JointTransform;
import com.example.openglexemple.Animation.KeyFrame;
import com.example.openglexemple.Animation.Quaternion;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationLoader {

    private static final float[] CORRECTION = new float[16];
    private final XmlNode animationNode;
    private final String name;
    private final List<XmlNode> childAnimations;
    private KeyFrame[] keyFrames;
    private float[] times;
    private Map<String, float[][]> animationData = new HashMap<>();
    public AnimationLoader(XmlNode animationNode, String rootJointId){
        this.animationNode = animationNode;
        name = animationNode.getAttribute("name");
        childAnimations = animationNode.getChildren("animation");
        Matrix.setIdentityM(CORRECTION, 0);
        Matrix.rotateM(CORRECTION, 0, (float) Math.toRadians(-90), 1.0f, 0.0f, 0.0f);
        createTimes();
        getAnimationTransforms(rootJointId);
        createKeyFrames();
    }

    public Animation createAnimation(){
        return new Animation(name, times, keyFrames);
    }

    private void createTimes(){
        XmlNode animation = childAnimations.get(0);
        XmlNode sampler = animation.getChild("sampler");
        String inputSourceStr = sampler.getChildWithAttribute("input", "semantic","INPUT").getAttribute("source");
        XmlNode inputSource = animation.getChildWithAttribute("source", "id", inputSourceStr.substring(1));
        times = Utils.stringToFloatArray(inputSource.getChild("float_array").getData());
    }
    private void getAnimationTransforms(String rootJointId){
        for(XmlNode animation : childAnimations){
            extractAnimationTransforms(animation, rootJointId );
        }
    }

    private void extractAnimationTransforms(XmlNode animationNode, String rootJointId){
        String jointId = getJointId(animationNode);
        String transformId = getTransformId(animationNode);
        XmlNode transformData = animationNode.getChildWithAttribute("source", "id", transformId);
        String transformStr = transformData.getChild("float_array").getData();
        String[] transformArr = transformStr.split("\\s+");
        processAnimation(jointId, transformArr, jointId.equals(rootJointId));
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
    private void processAnimation(String jointId, String[] transforms, Boolean isRootJoint){
        float[][] transformList = new float[times.length][16];

        for(int i=0; i < times.length;i++){
            float[] transform = new float[16];

            for(int j=0; j < 16; j++){
                transform[j] = Float.parseFloat(transforms[i * 16 + j]);
            }

            if(isRootJoint){
                //because up axis in Blender is different to up axis in game
                //Matrix.multiplyMM(transform, 0, CORRECTION, 0,transform,0);
            }

            float[] transpose = new float[16];
            Matrix.transposeM(transpose,0, transform, 0);

            transformList[i] = transpose;
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
                float[] poseMatrix = ad.getValue()[i];
                JointTransform jointAnimationTransform = createTransform(poseMatrix);
                jointKeyFrames.put(jointId, jointAnimationTransform);
            }
            keyFrames[i] = new KeyFrame(times[i], jointKeyFrames);
        }
    }
    private static JointTransform createTransform(float[] t) {
        Vector3f translation = new Vector3f(t[12], t[13], t[14]);
        Quaternion rotation = new Quaternion(t);
        return new JointTransform(translation, rotation);
    }
}
