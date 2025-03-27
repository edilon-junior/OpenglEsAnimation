package com.example.openglexemple.Engine;

import com.example.openglexemple.Math.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f rotation;
    private Vector3f cameraTarget;
    private Vector3f cameraDirection;
    private float sensibility = 10;
    public Camera(){
        position = new Vector3f();
        rotation = new Vector3f();
        cameraTarget = new Vector3f();
        cameraDirection = new Vector3f();
    }

    public Vector3f getPosition(){
        return this.position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x,y,z);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX * sensibility;
        rotation.y += offsetY * sensibility;
        rotation.z += offsetZ * sensibility;
    }

    public void update(Input input){
        moveRotation(input.getMovement()[0], input.getMovement()[1],0);
    }

}
