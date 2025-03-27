package com.example.openglexemple.Engine;

import android.opengl.Matrix;

import com.example.openglexemple.Engine.Camera;
import com.example.openglexemple.Math.Vector3f;

public class Transformation {
    private static final String TAG = "TRANSFORMATION";
    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;
    private final float[] viewMatrix = new float[16];
    private final float[] currentViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];

    public Transformation(){

    }

    public void createViewMatrix(){
        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        System.arraycopy(viewMatrix, 0, currentViewMatrix, 0, viewMatrix.length);
    }

    public void createPerspectiveMatrix(int width, int height){
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 100.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public float[] convertIntoEyeSpace(float[] vector, float[] modelMatrix){
        float[] tempMatrix = new float[16];
        float[] result = new float[4];

        Matrix.multiplyMV(tempMatrix, 0, modelMatrix, 0, vector, 0);
        Matrix.multiplyMV(result, 0, currentViewMatrix, 0, tempMatrix, 0);

        return result;
    }

    public void createViewProjectionMatrix(){
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, currentViewMatrix, 0);
    }

    public float[] getMPMatrix(float[] modelMatrix){
        float[] mMPMatrix = new float[16];

        Matrix.multiplyMM(mMPMatrix, 0, projectionMatrix, 0,modelMatrix, 0);

        return mMPMatrix;
    }

    public float[] getMVPMatrix(float[] modelMatrix){
        float[] mMVPMatrix = new float[16];

        Matrix.multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0,modelMatrix, 0);

        return mMVPMatrix;
    }

    public void updateViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        System.arraycopy(viewMatrix, 0, currentViewMatrix, 0, viewMatrix.length);

        // First do the rotation so camera rotates over its position
        Matrix.rotateM(currentViewMatrix,0,(float)Math.toRadians(rotation.y),1,0,0);
        Matrix.rotateM(currentViewMatrix,0,(float)Math.toRadians(rotation.x),0, 1, 0);
        // Then do the translation
        Matrix.translateM(currentViewMatrix,0,-cameraPos.x, -cameraPos.y, cameraPos.z);
    }
    public float[] getViewMatrix(){
        return this.currentViewMatrix;
    }

    public float[] getProjectionMatrix(){
        return this.projectionMatrix;
    }

    public float[] getViewProjectionMatrix(){return this.viewProjectionMatrix;}

    public float[] orbit(Vector3f p, Vector3f c, Vector3f t, float theta) {

        //normalize t
        t = t.getNormalized();

        float orbitAngleRad = (float) Math.toRadians(theta);

        double sinTheta = Math.sin(orbitAngleRad);
        double cosTheta = Math.cos(orbitAngleRad);

        float x0 = p.x;
        float y0 = p.y;
        float z0 = p.z;

        float x = (float) ((c.x*(t.y*t.y + t.z*t.z) - t.x*(c.y*t.y + c.z*t.z - t.dot(p)))*(1-cosTheta)
                + x0*cosTheta + (-c.z*t.y + c.y*t.z - t.z*y0 + t.y*z0)*sinTheta);

        float y = (float) ((c.y*(t.x*t.x + t.z*t.z) - t.y*(c.x*t.x + c.z*t.z - t.dot(p)))*(1-cosTheta)
                + y0*cosTheta + (c.z*t.x - c.x*t.z + t.z*x0 - t.x*z0)*sinTheta);

        float z = (float) ((c.z*(t.x*t.x + t.y*t.y) - t.z*(c.x*t.x + c.y*t.y - t.dot(p)))*(1-cosTheta)
                + z0*cosTheta + (-c.y*t.x + c.x*t.y - t.y*x0 + t.x*y0)*sinTheta);

        return new float[]{x,y,z};
    }

}
