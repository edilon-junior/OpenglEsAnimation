package com.example.openglexemple.Utils;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

public class Saver {
    public static final String TAG = "SAVER";
    private final StringBuilder stringBuilder = new StringBuilder();
    private final String fileName;
    public Saver(String fileName){
        this.fileName = fileName;
    }

    public void addGeometry(
            float[] positions,
            float[] normals,
            float[] textures,
            float[] colors){
        stringBuilder.append("positions \n");
        if(positions != null) {
            for (int i = 0; i < positions.length / 3; i++) {
                stringBuilder.append(i).append(" ").append(positions[i * 3]).append(", ");
                stringBuilder.append(positions[i * 3 + 1]).append(", ");
                stringBuilder.append(positions[i * 3 + 2]).append("\n");
            }
        }
        if(normals != null) {
            stringBuilder.append("\n");
            stringBuilder.append("normals \n");
            for (int i = 0; i < normals.length / 3; i++) {
                stringBuilder.append(i).append(" ").append(normals[i * 3]).append(", ");
                stringBuilder.append(normals[i * 3 + 1]).append(", ");
               stringBuilder.append(normals[i * 3 + 2]).append("\n");
            }
        }
        if(textures != null) {
            stringBuilder.append("\n");
            stringBuilder.append("textures \n");
            for (int i = 0; i < textures.length / 2; i++) {
                stringBuilder.append(i).append(" ").append(textures[i * 2]).append(", ");
                stringBuilder.append(textures[i * 2 + 1]).append("\n");
            }
        }
        if(colors != null){
            stringBuilder.append("\n");
            stringBuilder.append("colors \n");
            for (int i = 0; i < colors.length / 3; i++) {
                stringBuilder.append(i).append(" ").append(colors[i * 3]).append(", ");
                stringBuilder.append(colors[i * 3 + 1]).append(", ");
                stringBuilder.append(colors[i * 3 + 2]).append(",");
                stringBuilder.append(colors[i * 3 + 3]).append("\n");
            }
        }
    }

    public void addIndices(short[] indices){
        stringBuilder.append("indices : [");
        for(short i : indices){
            stringBuilder.append(i).append(" ");
        }
        stringBuilder.append("]");
    }

    public void addVertices(float[] vertices, String[] semantics, int stride){

        boolean hasColor = false;
        int inc = 0;
        for(String s : semantics){
            if(s.equalsIgnoreCase("COLOR")){
                hasColor = true;
                inc = 4;
            }
        }

        for(int i =0; i < vertices.length/stride; i++){
            stringBuilder.append("v[").append(vertices[i*stride]).append(" ");
            stringBuilder.append(vertices[i * stride + 1]).append(" ");
            stringBuilder.append(vertices[i * stride + 2]).append(" ");
            stringBuilder.append(vertices[i * stride + 3]).append("] ");
            stringBuilder.append("n[").append(vertices[i * stride + 4]).append(" ");
            stringBuilder.append(vertices[i * stride + 5]).append(" ");
            stringBuilder.append(vertices[i * stride + 6]).append(" ");
            stringBuilder.append(vertices[i * stride + 7]).append("] ");
            stringBuilder.append("t[").append(vertices[i * stride + 8]).append(" ");
            stringBuilder.append(vertices[i * stride + 9]).append("]");
            if(hasColor){
                stringBuilder.append(" c[").append(vertices[i * stride + 10]).append(" ");
                stringBuilder.append(vertices[i * stride + 11]).append(" ");
                stringBuilder.append(vertices[i * stride + 12]).append(" ");
                stringBuilder.append(vertices[i * stride + 13]).append("] ");
            }
            if(stride > 14) {
                stringBuilder.append(" j[");
                stringBuilder.append(vertices[i * stride + 10 + inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 11 + inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 12 + inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 13 + inc]).append("] ");
                stringBuilder.append("w[");
                stringBuilder.append(vertices[i * stride + 14+ inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 15+ inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 16+ inc]).append(" ");
                stringBuilder.append(vertices[i * stride + 17+ inc]).append("]");
            }
            stringBuilder.append("\n");
        }
    }

    public void addKeyFrames(KeyFrame[] keyFrames){
        for(int i = 0 ; i<keyFrames.length; i++) {
            stringBuilder.append("keyFrame ").append(i).append(" ");
            stringBuilder.append(keyFrames[i]);
            stringBuilder.append("\n");
        }
    }
    
    public String getVerticesString(){
        return stringBuilder.toString();
    }

    public void writeFileOnInternalStorage(Context context){
        String content = getVerticesString();
        writeFileOnInternalStorage(context, content);
    }

    public void writeFileOnInternalStorage(Context context, String content){
        File dir = new File(context.getFilesDir(), "game_data");
        if(!dir.exists()){
            dir.mkdir();
        }
        System.out.println("########### data path: "+ dir.getAbsolutePath());
        try {
            File file = new File(dir, fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
