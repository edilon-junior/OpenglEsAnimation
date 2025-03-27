package com.example.openglexemple;

public class TextHandler {

    private int cols;
    private int rows;

    private float[][] uvCoords;

    public TextHandler(int cols, int rows){
        this.cols = cols;
        this.rows = rows;

    }

    public float[][] createUVCoords(int[] fontPos){
        float[][] textCoords = new float[6][2];
        float dc = (float) 1 /cols;
        float dr = (float) 1 /rows;
        textCoords[0][0] = dc*(fontPos[0]);
        textCoords[0][1] = dr*(fontPos[1]);

        textCoords[1][0] = dc*(fontPos[0]+1);
        textCoords[1][1] = dr*(fontPos[1]);

        textCoords[2][0] = dc*(fontPos[0]);
        textCoords[2][1] = dr*(fontPos[1]+1);

        textCoords[3][0] = dc*(fontPos[0]+1);
        textCoords[3][1] = dr*(fontPos[1]);

        textCoords[4][0] = dc*(fontPos[0]+1);
        textCoords[4][1] = dr*(fontPos[1]+1);

        textCoords[5][0] = dc*(fontPos[0]);
        textCoords[5][1] = dr*(fontPos[1]+1);

        return textCoords;
    }

}
