package com.example.openglexemple.Math;

import static java.lang.Float.NaN;

public class LineEquation {

    private final Vector3f initialPosition = new Vector3f();
    private final Vector3f direction = new Vector3f();

    public LineEquation(Vector3f initialPosition, Vector3f direction){
        this.initialPosition.set(initialPosition);
        this.direction.set(direction);
    }

    public void set(Vector3f initialPosition, Vector3f direction) {
        this.initialPosition.set(initialPosition);
        this.direction.set(direction);
    }

    public float getParameter(Vector3f position){
        if(direction.x != 0){
            return (position.x - initialPosition.x)/direction.x;
        }
        if(direction.y != 0){
            return (position.y - initialPosition.y)/direction.y;
        }
        if(direction.z != 0){
            return (position.z - initialPosition.z)/direction.z;
        }
        return NaN;
    }

    public Vector3f getInitialPosition(){
        return initialPosition;
    }

    public Vector3f getDirection(){
        return direction;
    }

    public Vector3f getPosition(float parameter){
        return initialPosition.getAdd(direction.getMul(parameter));
    }

    public float distance(Vector3f point){
        Vector3f d = initialPosition.getSub(point);
        Vector3f ortoProj = d.ortoProj(direction);
        return ortoProj.length();
    }

    public float distance(LineEquation line){
        Vector3f normal = this.direction.cross(line.direction);
        Vector3f dp = this.initialPosition.getSub(line.initialPosition);
        Vector3f projDpOnNormal = dp.proj(normal);
        return projDpOnNormal.length();
    }

    @Override
    public String toString(){
        return initialPosition.toString()+" "+direction.toString();

    }
}
