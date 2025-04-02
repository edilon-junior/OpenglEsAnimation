package com.example.openglexemple.GraphicObjects;

import android.opengl.Matrix;

import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.Engine.Timer;

public class Light {
    private final float[] lightColor = new float[]{1, 1, 1};
    private final float[] lightPosInWordSpace = new float[]{10,10,1,1};
    private final float[] lightModelMatrix = new float[16];
    private float[] lightPosInEyeSpace = new float[4];
    private float intensity = 0.5f;

    public Light(Transformation transformation){
        Matrix.setIdentityM(lightModelMatrix, 0);
        lightPosInEyeSpace = transformation.convertIntoEyeSpace(lightPosInWordSpace, lightModelMatrix);
    }

    public void setLightColor(float[] light){
        lightColor[0] = light[0];
        lightColor[1] = light[1];
        lightColor[2] = light[2];
    }

    public float[] getLightColor() {
        return lightColor;
    }

    public float[] getLightPosInEyeSpace() {
        return lightPosInEyeSpace;
    }

    public void setLightPosInEyeSpace(float[] lightPosInEyeSpace) {
        this.lightPosInEyeSpace = lightPosInEyeSpace;
    }

    public void update(Timer timer){

    }
    public void setIntensity(float intensity){
        this.intensity = intensity;
    }
    public float[] getIntensity(){
        return new float[]{intensity};
    }
}
