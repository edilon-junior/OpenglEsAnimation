package com.example.openglexemple.Math;

import androidx.annotation.NonNull;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(){
        set(0,0,0);
    }

    public Vector3f(Vector3f v){
        set(v.x, v.y, v.z);
    }

    public Vector3f(float x, float y, float z ){
        set(x,y,z);
    }

    public Vector3f(float[] vec){
        set(vec[0], vec[1], vec[2]);
    }

    public void set(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float[] vec){
        this.x = vec[0];
        this.y = vec[1];
        this.z = vec[2];
    }

    public void set(Vector3f v){
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    @NonNull
    public Vector3f clone(){
        return new Vector3f(this);
    }

    /**
     *
     * @return unit vector of this
     */
    public void normalize(){
        float length = this.length();
        this.mul(1/length);
    }

    public Vector3f getNormalized(){
        return clone().getMul(1/ this.length());
    }

    public void add(Vector3f v){
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void add(float x, float y, float z){
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public Vector3f getAdd(Vector3f v){
        Vector3f vec = clone();
        vec.add(v);
        return vec;
    }

    public void sub(Vector3f v){
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public Vector3f getSub(Vector3f v){
        Vector3f vec = clone();
        vec.sub(v);
        return vec;
    }

    //multiply by escalar
    public void mul(float k){
        this.x *= k;
        this.y *= k;
        this.z *= k;
    }

    public Vector3f getMul(float k){
        Vector3f vec = clone();
        vec.mul(k);
        return vec;
    }

    public Vector3f getTransform(float[] mat){
        Vector3f result = clone();
        result.transform(mat);
        return result;
    }

    public void transform(float[] mat){
        float w = 1.0f;
        float tx = mat[0]*x + mat[4]*y + mat[8]*z + mat[12]*w;
        float ty = mat[1]*x + mat[5]*y + mat[9]*z + mat[13]*w;
        float tz = mat[2]*x + mat[6]*y + mat[10]*z + mat[14]*w;
        float tw = mat[3]*x + mat[7]*y + mat[11]*z + mat[15]*w;
        set(tx,ty, tz);
    }

    public float dot(Vector3f v){
        return this.x*v.x + this.y*v.y + this.z*v.z;
    }

    public Vector3f cross(Vector3f v){
        float x = this.y*v.z - this.z*v.y;
        float y = this.z*v.x - this.x*v.z;
        float z = this.x*v.y - this.y*v.x;
        return new Vector3f(x,y,z);
    }

    public float length(){
        return (float) Math.sqrt(this.dot(this));
    }

    public Vector3f proj(Vector3f v){
        return v.getMul(this.dot(v)/v.dot(v));
    }

    public Vector3f ortoProj(Vector3f v){
        return getSub(proj(v));
    }

    public float cosine(Vector3f vector){
        return this.dot(vector)/(this.length()*vector.length());
    }

    public boolean isParallel(Vector3f vector){
        float cosine = this.cosine(vector);
        if(cosine == 1 || cosine == -1){
            return true;
        }
        return false;
    }

    public boolean isPerpendicular(Vector3f vector){
        float cosine = this.cosine(vector);
        if(cosine == 0){
            return true;
        }
        return false;
    }

    public float[] toFloat(){
        return new float[]{this.x, this.y, this.z};
    }

    public float[] toFloat4(){return new float[]{this.x, this.y, this.z, 1.0f};}

    public boolean equals(Vector3f v){
        return x == v.x && y == v.y && z == v.z;
    }

    public String toString(){
        return new String("("+x+", "+y+", "+z+")");
    }
}
