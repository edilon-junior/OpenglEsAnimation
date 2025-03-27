package com.example.openglexemple.GameObjects;

import android.opengl.Matrix;

import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.Engine.Timer;

public class Light {
    private final float[] lightColor = new float[]{1, 1, 1};
    private final float[] lightPosInWordSpace = new float[]{1,1,1,1};
    private final float[] lightModelMatrix = new float[16];
    private float[] lightPosInEyeSpace = new float[4];

    private float strength = 0.1f;

    public Light(Transformation transformation){
        Matrix.setIdentityM(lightModelMatrix, 0);
        lightPosInEyeSpace = transformation.convertIntoEyeSpace(lightPosInWordSpace, lightModelMatrix);
    }

    public void setLightColor(float[] light){
        this.lightColor[0] = light[0];
        this.lightColor[1] = light[1];
        this.lightColor[2] = light[2];
    }

    public float[] getLightColor() {
        return lightColor;
    }

    public float[] getLightPosInEyeSpace() {
        return lightPosInEyeSpace;
    }

    public void setLightPosInEyeSpace(float[] mLightPosInEyeSpace) {
        this.lightPosInEyeSpace = mLightPosInEyeSpace;
    }

    public void update(Timer timer){

    }
}
