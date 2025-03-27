package com.example.openglexemple;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.Timer;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GameObjects.GameObject;

import java.util.Map;

public class Text extends GameObject {

    private static final float ZPOS = 0.0f;

    private static final int VERTICES_LENGTH = 4;

    private int texture;
    private String text;

    public Text(int texture, String text, int cols, int rows) {
        super();
        this.texture = texture;
        this.text = text;
        createMesh(text, cols, rows);
    }

    public void createMesh(String text, int cols, int rows){
        float[] positions = new float[(text.length()+1)*2*Constants.POSITION_SIZE];

        for(int i=0; i< positions.length;i++){

        }
    }

    @Override
    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input, Map<String, GameObject> gameObjectMap) {

    }
}
