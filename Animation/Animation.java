package com.example.openglexemple.Animation;

import android.opengl.Matrix;

import com.example.openglexemple.Utils.Utils;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Loader.XmlParser.XmlNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {

    public static final String TAG = "ANIMATION";
    String id;
    String name;
    float[] times;
    public KeyFrame[] keyFrames;
    float animationTime = 0;
    private final float duration;;

    public Animation(String name,float[] times, KeyFrame[] keyFrames){
        this.name = name;
        this.keyFrames = keyFrames;
        duration = times[times.length-1];
    }

    public void increaseAnimationTime(float time) {
        animationTime += time;
        if (animationTime > duration) {
            this.animationTime %= duration;
        }
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

    public void applyPoseToJoints(Map<String, float[]> currentPose, Joint joint, float[] parentTransform, float[][] test, Skeleton skeleton) {
        float[] currentLocalTransform = currentPose.get(joint.getId());
        float[] currentTransform = new float[16];

        if(currentLocalTransform == null){
            Joint j = skeleton.getJoints().get(joint.getId());
            if(j != null){
                currentLocalTransform = j.getTransformMatrix();
            }else {
                currentLocalTransform = new float[16];
                Matrix.setIdentityM(currentLocalTransform, 0);
            }
        }

        Matrix.multiplyMM(currentTransform, 0,parentTransform, 0, currentLocalTransform, 0);

        for (Joint childJoint : joint.getChildren()) {
            applyPoseToJoints(currentPose, childJoint, currentTransform, test, skeleton);
        }

        float[] rootIbmXpose = new float[16];
        float[] poseXibm = new float[16];
        Matrix.multiplyMM(rootIbmXpose, 0, skeleton.getRootJoint().getIBM(), 0, currentTransform, 0);
        Matrix.multiplyMM(poseXibm,0, rootIbmXpose, 0, joint.getIBM(), 0);

        joint.setAnimatedTransform(poseXibm);
        test[joint.getIndex()]= poseXibm;
    }

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

}
