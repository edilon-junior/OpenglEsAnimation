package com.example.openglexemple.GraphicObjects;

import static com.example.openglexemple.Constants.POINT_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.POINT_INT_UNIFORMS;

import android.opengl.GLES30;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GameObjects.GameObject;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Point extends GameObject {

    public Point(Vector3f position, float[] color, float size, ShaderProgram shaderProgram){
        super();
        setShaderProgramId(shaderProgram.getProgramHandle());
        String[] semantics = new String[]{"VERTEX"};
        setPosition(position);
        setColor(color);
        setPointSize(size);
        Mesh pointMesh = new Mesh(new float[]{0,0,0}, null, null, null, null, null);
        pointMesh.setSemantics(semantics);
        pointMesh.setupNonInterleavedMesh(shaderProgram);
        List<Mesh> meshes = new ArrayList<>();
        meshes.add(pointMesh);
        setMesh(meshes);
        setUpdateModelMatrix(true);
    }

    @Override
    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input, Map<String, GameObject> gameObjectMap) {
        updateTranslation(timer);
        updateRotation(timer);
        updateScale();
        if(isUpdateModelMatrix()) {
            updateModelMatrix(transformation);
        }
    }

    @Override
    public void render(ShaderProgram shaderProgram, Transformation transformation){
        shaderProgram.passFloatUniforms(POINT_FLOAT_UNIFORMS, getMvpMatrix(), getColor(), getPointSize());
        shaderProgram.passIntUniforms(POINT_INT_UNIFORMS, new int[]{isSelected()});
        for(Mesh mesh: getMeshList()){
            mesh.render(GLES30.GL_POINTS, 0, 0);
        }
    }
}


