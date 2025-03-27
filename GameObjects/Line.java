package com.example.openglexemple.GameObjects;

import static com.example.openglexemple.Constants.POINT_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.POINT_INT_UNIFORMS;
import static com.example.openglexemple.Constants.POSITION_SIZE;

import android.opengl.GLES30;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.Math.LineEquation;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Line extends GameObject {

    public static final String TAG = "LINE";
    private Vector3f[] vertices;
    private float[] positions;
    private short[] indices;
    private LineEquation[] lineEquations;

    /**
     * @param vertices: define vertices of the polyline
     * @param color: color of lines
      * @param shaderProgram: shader program used by this object
     **/

    public Line(Vector3f[] vertices, float[] color, ShaderProgram shaderProgram){
        super();
        this.vertices = vertices;
        setPosition(vertices[0]);
        setShaderProgramId(shaderProgram.getProgramHandle());
        setColor(color);
        setScale(new Vector3f(1, 1, 1));

        createArrays(vertices);

        //-------------create and setup mesh ------------
        Mesh lineMesh = new Mesh(positions, null, null, null, null, indices);
        lineMesh.setSemantics(new String[]{"VERTEX"});
        lineMesh.setupNonInterleavedMesh(shaderProgram);
        List<Mesh> meshes = new ArrayList<>();
        meshes.add(lineMesh);
        setMesh(meshes);
        setUpdateModelMatrix(true);
    }

    private void createArrays(Vector3f[] vertices){
        positions = new float[vertices.length * POSITION_SIZE];
        indices = new short[vertices.length];
        lineEquations = new LineEquation[vertices.length-1];

        for(int i=0; i < vertices.length; i++){
            indices[i] = (short) i;

            positions[i*POSITION_SIZE] = vertices[i].x;
            positions[i*POSITION_SIZE + 1] = vertices[i].y;
            positions[i*POSITION_SIZE + 2] =  vertices[i].z;

            if(i < (vertices.length-1)) {
                lineEquations[i] = new LineEquation(vertices[i], vertices[i + 1].getSub(vertices[i]));
            }
        }
    }

    private int selectTest(LineEquation line){
        for(LineEquation l : lineEquations) {
            float distance = l.distance(line);
            if (distance <= 0.15) {
                return 1;
            }
        }
        return 0;
    }

    public void update(Timer timer,
                       Transformation transformation,
                       MousePicker mousePicker,
                       Input input,
                       Map<String, GameObject> gameObjectMap){

        updateBonds(gameObjectMap);
        //if(input.getMovement()[0] != 0 && input.getMovement()[1] != 0){
          //  setUpdateModelMatrix(true);
        //}
        updateTranslation(timer);
        updateRotation(timer);
        updateScale();
        if(isUpdateModelMatrix()) {
            updateModelMatrix(transformation);
        }

        //if(input.isTouch() && isTangible()){
          //  isSelected = selectTest(mousePicker.getLine());
        //}

    }

    @Override
    public void render(ShaderProgram shaderProgram, Transformation transformation){
        shaderProgram.passFloatUniforms(POINT_FLOAT_UNIFORMS, getMvpMatrix(), getColor(), getPointSize());
        shaderProgram.passIntUniforms(POINT_INT_UNIFORMS, new int[]{isSelected()} );
        for(Mesh mesh: getMeshList()){
            mesh.render(GLES30.GL_LINE_LOOP, 0, 0);
        }
    }
}
