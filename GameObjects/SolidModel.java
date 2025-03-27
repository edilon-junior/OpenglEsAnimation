package com.example.openglexemple.GameObjects;

import static com.example.openglexemple.Constants.BYTES_PER_SHORT;
import static com.example.openglexemple.Constants.STATIC_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.STATIC_INT_UNIFORMS;

import android.opengl.GLES30;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Timer;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GameObjects.GameObject;
import com.example.openglexemple.GameObjects.Light;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.GraphicObjects.Scene;

import java.util.Map;

public class SolidModel extends GameObject {
    private static final String TAG = "SOLID_MODEL";
    /**
     * @param modelName the name of this model
     * @param shaderProgram the shader program to this model
     */
    public SolidModel(String modelName, ShaderProgram shaderProgram) {
        super(); //call default constructor
        setName(modelName);
        setShaderProgramId(shaderProgram.getProgramHandle());
    }

    @Override
    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input) {
        if(getAngularVelocity()>0) {
            updateRotation(timer);
        }
        updateModelMatrix(transformation);
    }

    @Override
    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input, Map<String, GameObject> gameObjectMap) {

    }

    @Override
    public void render(Scene scene, Transformation transformation){
        for(Mesh mesh: this.getMeshList()) {
            if(mesh == null) return; //do nothing

            int fromIndex = 0;
            int mIndex = 0;

            for(Material material: mesh.getMaterials()) {

                float[] ambient = material.getAmbient();
                float[] diffuse = material.getDiffuse();
                float[] specular = material.getSpecular();
                float[] shininess = material.getShininess();

                Light light = scene.getLight();
                ShaderProgram shaderProgram = scene.getShaderPrograms()
                        .get(getShaderProgramId());

                material.setupSampler2d(mIndex);

                if (shaderProgram != null) {
                    shaderProgram.passIntUniforms(STATIC_INT_UNIFORMS, material.getSample2Did());
                    shaderProgram.passFloatUniforms(STATIC_FLOAT_UNIFORMS, light.getLightPosInEyeSpace(), light.getLightColor(), new float[]{0, 0, 0},
                            ambient, diffuse, specular, shininess,
                            transformation.getViewMatrix(), getMvpMatrix());
                }
                //arithmetic progression: a_n = a_0 + n*r; a_0 = 0
                int toIndex = mesh.getIndices().length;
                mesh.render(GLES30.GL_TRIANGLES, toIndex, fromIndex * BYTES_PER_SHORT);

                fromIndex = fromIndex + toIndex;

                mIndex++;
            }
        }
    }
}


