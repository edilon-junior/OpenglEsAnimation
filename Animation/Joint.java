package com.example.openglexemple.Animation;

import android.opengl.Matrix;

import java.util.ArrayList;

public class Joint {
    public static final String TAG = "JOINT";
    private final Integer index;
    private String id;
    private String name;
    private String parentId;
    private final ArrayList<Joint> children = new ArrayList<>();
    private final float[] inverseBindMatrix = new float[16];
    private final float[] localTransform;
    private final float[] animatedTransform = new float[16];

    public Joint(String id, String name, int index, float[] localTransform){
        this.id = id;
        this.name = name;
        this.index = index;
        this.localTransform = localTransform;
    }

    public void calcInverseBindTransform(float[] parentBindTransform) {
        float[] bindTransform = new float[16];
        Matrix.multiplyMM(bindTransform,0,parentBindTransform, 0, localTransform, 0);
        float[] ibm = new float[16];
        Matrix.invertM(ibm, 0, bindTransform, 0);
        for (Joint child : children) {
            child.calcInverseBindTransform(bindTransform);
        }
        setIBM(ibm);
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setIBM(float[] ibm){
        System.arraycopy(ibm, 0, inverseBindMatrix, 0, ibm.length);
    }
    public float[] getIBM() {
        return inverseBindMatrix;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parent) {
        this.parentId = parent;
    }

    public ArrayList<Joint> getChildren() {
        return children;
    }

    public void addChild(Joint child) {
        this.children.add(child);
    }

    public float[] getTransformMatrix() {
        return localTransform;
    }

    public float[] getAnimatedTransform() {
        return animatedTransform;
    }

    public void setAnimatedTransform(float[] animatedTransform) {
        System.arraycopy(animatedTransform, 0, this.animatedTransform, 0, animatedTransform.length);
    }

    public void setLocalTransform(float[] floats) {
    }
}
