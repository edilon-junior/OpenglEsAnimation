package com.example.openglexemple;

public class Messenger {
    private boolean booleanVal = false;
    private int intVal = 0;
    private float floatVal = 0;
    private float[] floatArray;
    private int[] intArray;
    private byte byteVal = 0;
    private final byte type;

    public Messenger(byte byteVal){
        type = Constants.TYPE_BYTE;
        this.byteVal = byteVal;
    }
    public Messenger(float floatVal){
        type = Constants.TYPE_FLOAT;
        this.floatVal = floatVal;
    }
    public Messenger(float[] floatArray){
        type = Constants.TYPE_FLOAT_ARRAY;
        this.floatArray = floatArray;
    }
    public Messenger(boolean booleanVal){
        type = Constants.TYPE_BOOLEAN;
        this.booleanVal = booleanVal;
    }
    public Messenger(int intVal){
        type = Constants.TYPE_INT;
        this.intVal = intVal;
    }

    public byte getType(){
        return type;
    }

    public boolean getBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public float[] getFloatArray() {
        return floatArray;
    }

    public void setFloatArray(float[] floatArray) {
        this.floatArray = floatArray;
    }
    public byte getByteVal(){
        return byteVal;
    }
    public void setByteVal(byte byteVal){
        this.byteVal = byteVal;
    }

}
