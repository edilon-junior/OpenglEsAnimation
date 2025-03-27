package com.example.openglexemple.ColladaParser;

import android.os.CpuUsageInfo;

public class Vertex {
    private int index = -1;
    private int position = -1;
    private int normal = -1;
    private int uv = -1;
    private int color = -1;
    private int jointsId = -1;
    private int weights = -1;
    private Vertex duplicateVertex;

    public Vertex(int index, int position){
        this.index = index;
        this.position = position;
    }
    public void setIndex(int index){this.index = index;}
    public void setPosition(int position){
        this.position = position;
    }
    public void setNormal(int normal){
        this.normal = normal;
    }
    public void setUv(int uv){
        this.uv = uv;
    }
    public void setColor(int color){this.color = color;}
    public void setJointsId(int jointsId){
        this.jointsId = jointsId;
    }
    public void setWeights(int weights){
        this.weights = weights;
    }
    public int getIndex(){return index;}
    public int getPosition() {
        return position;
    }

    public int getNormal() {
        return normal;
    }

    public int getUv() {
        return uv;
    }

    public int getColor() {
        return color;
    }

    public int getJointsId() {
        return jointsId;
    }

    public int getWeights() {
        return weights;
    }

    public boolean isSet(){
        return normal != -1 && uv!= -1 && color != -1;
    }
    public boolean equals(int n, int t, int c){
        return normal == n && uv == t && color == c;
    }

    public Vertex getDuplicateVertex() {
        return duplicateVertex;
    }

    public void setDuplicateVertex(Vertex duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }
    @Override
    public String toString(){
        return "i="+index+" p="+position+" n="+normal+" t="+uv+" c="+color;
    }
}
