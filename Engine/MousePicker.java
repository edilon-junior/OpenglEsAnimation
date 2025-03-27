package com.example.openglexemple.Engine;

import android.opengl.Matrix;

import com.example.openglexemple.Engine.Camera;
import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Math.LineEquation;
import com.example.openglexemple.Math.Vector3f;

public class MousePicker {

    private final String TAG = "MOUSE_PICKER";

    private final float zDir = -1.0f;
    private Vector3f origin = new Vector3f();
    private Vector3f currentRay;
    private Camera camera;

    private Vector3f eye;
    private LineEquation line;

    public MousePicker(Camera camera){
        this.camera = camera;
        this.line = new LineEquation(origin, new Vector3f(0,0,1.0f));
    }

    public LineEquation getLine(){return line;}
    public Vector3f getCurrentRay(){
        return currentRay;
    }

    public void update(float[] viewMatrix, float[] projMatrix, Input input){
        if(input.isTouch()==false){
            return;
        }
        this.currentRay = calcMouseRay(input, viewMatrix, projMatrix);
        Vector3f direction = currentRay.getSub(camera.getPosition());
        origin.set(camera.getPosition());
        this.line.set(origin, direction);
    }
    private Vector3f calcMouseRay(Input input, float[] viewMatrix, float[] projMatrix){
        float[] normalizedOrigin = getNormalizedDeviceCoords(input);
        float[] clipCoords = new float[]{normalizedOrigin[0], normalizedOrigin[1], zDir, 1f};
        float[] eyesCoords = toEyeCoords(clipCoords, projMatrix);
        Vector3f worldRay = toWorldCoords(eyesCoords, viewMatrix);
        return worldRay;
    }

    private float[] toEyeCoords(float[] clipCoords, float[] projectionMatrix){
        float[] invProjection = new float[16];
        Matrix.invertM(invProjection, 0, projectionMatrix, 0);
        float[] eyesCoord = new float[4];
        Matrix.multiplyMV(eyesCoord, 0, invProjection, 0, clipCoords,0);
        return new float[]{eyesCoord[0], eyesCoord[1], zDir, 0};
    }

    private Vector3f toWorldCoords(float[] eyesCoords, float[] viewMatrix){
        float[] invViewMatrix = new float[16];
        Matrix.invertM(invViewMatrix, 0, viewMatrix, 0);
        float[] rayWorld = new float[4];
        Matrix.multiplyMV(rayWorld, 0, invViewMatrix, 0, eyesCoords, 0);
        Vector3f mouseRay = new Vector3f(rayWorld[0], rayWorld[1], rayWorld[2]);
        mouseRay.normalize();
        return mouseRay;
    }

    private float[] getNormalizedDeviceCoords(Input input){
        float mouseX = input.getDownCoordinates()[0];
        float mouseY = input.getDownCoordinates()[1];
        float x = (2f * mouseX) / input.getDisplaySize()[0] -1;
        float y = (2f * mouseY) / input.getDisplaySize()[1] -1;
        return new float[]{x,-y};
    }
}
