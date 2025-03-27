package com.example.openglexemple.Math;

public class PlaneEquation {
    private final String TAG = "PLANE_EQUATION";

    private Vector3f normal = new Vector3f();

    private Vector3f p0 = new Vector3f();
    private float normalLength;
    private float d = 0;//p is the perpendicular distance from the origin to the plane if n is unit vector.

    public PlaneEquation(Vector3f p0, Vector3f normal){
        this.normal.set(normal);
        this.p0.set(p0);
        this.d = -normal.dot(p0);
        this.normalLength = normal.length();
    }

    public PlaneEquation(Vector3f a, Vector3f b, Vector3f c){
        Vector3f ab = b.getSub(a);
        Vector3f ac = c.getSub(a);
        p0 = a;
        normal.set(ab.cross(ac));
        normalLength = normal.length();
        d = -normal.dot(a);
    }

    public PlaneEquation(PlaneEquation plane){
        set(plane);
    }

    public void set(PlaneEquation plane) {
        this.p0.set(plane.p0);
        this.normal.set(plane.normal);
        this.normalLength = plane.normalLength;
        this.d = plane.d;
    }

    public Vector3f getNormal(){
        return normal;
    }

    public float getConstantTerm(){
        return d;
    }

    public float getPosition(Vector3f point){
        return normal.dot(point) + d;
    }

    public void update(float[] matrix, float[] normalMatrix){
        this.normal.transform(normalMatrix);
        this.p0.transform(matrix);
        this.d = -normal.dot(p0);
        this.normalLength = normal.length();
    }

    public Vector3f intersection(LineEquation line){
        float cos = normal.dot(line.getDirection());
        //if cos = 0, then vectors are perpendicular, and the plane and line are parallels
        if(cos == 0){return null;}
        float parameter = -this.getPosition(line.getInitialPosition())/cos;
        return line.getPosition(parameter);
    }

    public float distance(Vector3f point){
        return Math.abs(getPosition(point))/normal.length();
    }

    public float distance(PlaneEquation plane){
        if(normal.isParallel(plane.getNormal()) == false){
            return 0;
        }
        return Math.abs(d - plane.getConstantTerm())/normal.length();
    }

    public boolean isEquivalent(PlaneEquation plane){
        float nLength = this.normal.length();
        float a = this.getConstantTerm() / nLength;
        float b = plane.getConstantTerm() / nLength;
        if(a == b){
            return true;
        }
        return false;
    }

    public String toString(){
        return normal.toString() + " + "+ d +", p0 = "+p0;
    }

}
