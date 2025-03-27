package com.example.openglexemple.Animation;

import com.example.openglexemple.Animation.JointTransform;

import java.util.Map;

public class KeyFrame {

    private final float timeStamp;
    private final Map<String, JointTransform> pose;

    /**
     * @param timeStamp
     *            - the time (in seconds) that this keyframe occurs during the
     *            animation.
     * @param jointKeyFrames
     *            - the local-space transforms for all the joints at this
     *            keyframe, indexed by the name of the joint that they should be
     *            applied to.
     */
    public KeyFrame(float timeStamp, Map<String, JointTransform> jointKeyFrames) {
        this.timeStamp = timeStamp;
        this.pose = jointKeyFrames;
    }

    /**
     * @return The time in seconds of the keyframe in the animation.
     */
    protected float getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return The desired bone-space transforms of all the joints at this
     *         keyframe, of the animation, indexed by the name of the joint that
     *         they correspond to. This basically represents the "pose" at this
     *         keyframe.
     */
    protected Map<String, JointTransform> getJointKeyFrames() {
        return pose;
    }


    public String toString(){
        StringBuilder mapAsString = new StringBuilder("{");
        mapAsString.append("time "+timeStamp+" [");
        for (String key : pose.keySet()) {
            mapAsString.append(key + "=" + pose.get(key).toString() + ", ");
        }
        mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("]}");
        return mapAsString.toString();
    }

}
