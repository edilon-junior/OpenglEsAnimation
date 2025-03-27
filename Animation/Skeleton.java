package com.example.openglexemple.Animation;

import com.example.openglexemple.Animation.Joint;

import java.util.HashMap;
import java.util.Map;

public class Skeleton {

    float[] bindShapeMatrix;
    Map<String, Joint> joints = new HashMap<>();

    final String rootJointId;

    int rootJointIndex;

    private Joint rootJoint;

    public Skeleton(String rootJointId){
        this.rootJointId = rootJointId;
    }

    public float[] getBindShapeMatrix() {
        return bindShapeMatrix;
    }

    public void setBindShapeMatrix(float[] bindShapeMatrix) {
        this.bindShapeMatrix = bindShapeMatrix;
    }

    public Map<String, Joint> getJoints() {
        return joints;
    }

    public void addJoints(Joint joint) {
        this.joints.put(joint.getId(), joint);
        if(joint.getId().equalsIgnoreCase(rootJointId)){
            rootJointIndex = joint.getIndex();
            rootJoint = joint;
        }
    }

    public String getRootJointId(){
        return rootJointId;
    }

    public Joint getRootJoint(){return rootJoint;}

}
