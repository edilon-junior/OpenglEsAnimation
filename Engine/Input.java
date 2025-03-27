package com.example.openglexemple.Engine;

public class Input {

    private final float[] downCoordinates = new float[2];
    private final float[] movement = new float[2];
    private boolean isLift = false;
    private boolean isTouch = false;
    private boolean isMove = false;
    private boolean up = false;
    private boolean right = false;
    private boolean down = false;
    private boolean left = false;
    private boolean pause = false;
    private float sensibilityFactor = 1;
    private float density;
    private float movementFactor;

    private int[] displaySize;

    public Input(){

    }
    public float[] getDownCoordinates() {
        return downCoordinates;
    }

    public void setDownCoordinates(float x, float y) {
        this.downCoordinates[0] = x;
        this.downCoordinates[1] = y;
    }

    public float[] getMovement() {
        return movement;
    }

    public void setMovement(float dx, float dy) {
        this.movement[0] = dx;
        this.movement[1] = dy;
    }

    public void addMovement(float dx, float dy){
        this.movement[0] += dx;
        this.movement[1] += dy;
    }
    public boolean isLift() {
        return isLift;
    }

    public void setLift(boolean lift) {
        isLift = lift;
    }

    public boolean isTouch() {
        return isTouch;
    }

    public void setTouch(boolean touch) {
        isTouch = touch;
    }
    public boolean isMove() {
        return isMove;
    }

    public void setMove(boolean move) {
        isMove = move;
    }
    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public float getSensibilityFactor() {
        return sensibilityFactor;
    }

    public void setSensibilityFactor(float sensibilityFactor) {
        this.sensibilityFactor = sensibilityFactor;
        setMovementFactor(sensibilityFactor);
    }

    public float getDisplayDensity(){
        return density;
    }
    public void setDisplayDensity(float density){
        this.density = density;
    }

    public float getMovementFactor(){
        return movementFactor;
    }
    public void setMovementFactor(float movementFactor){
        this.movementFactor = density / movementFactor;
    }
    public int[] getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int[] displaySize) {
        this.displaySize = displaySize;
    }

    public void cleanup(){
        setTouch(false);
        setDownCoordinates(0, 0);
        setMovement(0,0);
    }
}
