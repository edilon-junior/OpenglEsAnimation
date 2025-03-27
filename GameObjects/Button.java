package com.example.openglexemple.GameObjects;

import static com.example.openglexemple.Constants.BUTTON_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.BUTTON_INT_UNIFORMS;
import static com.example.openglexemple.Constants.BYTES_PER_SHORT;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.Math.LineEquation;
import com.example.openglexemple.Math.PlaneEquation;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.GraphicObjects.Scene;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Button extends GameObject {

    public static final String TAG = "BUTTON";
    private float radius;
    private float[] positions;
    private float[] normal;
    private short[] indices;
    private float[] meshVertices;
    private Vector3f[] vertices;
    private final ArrayList<Byte[]> inputBonds = new ArrayList<>();
    private Input input;
    private PlaneEquation plane;
    private PlaneEquation currentPlane;
    private Vector3f center = new Vector3f();
    private Vector3f currentCenter = new Vector3f();
    private Vector3f currentV0;
    private final float[] normalMatrix = new float[16];
    public Button(ShaderProgram shaderProgram){
        setScale(new Vector3f(1,1,1));
        createArrays();
        createMesh(shaderProgram);
        createPlane();
        setUpdateModelMatrix(true);
    }

    public void setInput(Input input){
        this.input = input;
    }

    private void createArrays(){
        vertices = new Vector3f[4];
        vertices[0] = new Vector3f(1,1,0);
        vertices[1] = new Vector3f(1,-1,0);
        vertices[2] = new Vector3f(-1,-1,0);
        vertices[3] = new Vector3f(-1,1,0);

        positions = new float[vertices.length*3];
        for(int i=0; i<vertices.length;i++){
            Vector3f v = vertices[i];
            positions[i * 3 ] = v.x;
            positions[i * 3 + 1] = v.y;
            positions[i * 3 + 2] = v.z;
        }
        //indices = new short[]{0,1,2,0,2,3};
        indices = new short[]{0,3,2,0,2,1};
        normal = new float[]{0,0,-1};
        meshVertices = new float[]{
                1,1,0,0,0,-1,0,1,
                1,-1,0,0,0,-1,0,0,
                -1,-1,0,0,0,-1,1,0,
                -1,1,0,0,0,-1,1,1
        };
    }

    public void createPlane(){
        plane = new PlaneEquation(vertices[0], new Vector3f(normal));
        currentPlane = new PlaneEquation(plane);
    }

    public void createMesh(ShaderProgram shaderProgram){
        setShaderProgramId(shaderProgram.getProgramHandle());
        Mesh mesh = new Mesh(null, null, null, null, meshVertices, indices);
        mesh.setSemantics(new String[]{"VERTEX", "NORMAL", "TEXCOORD"});
        mesh.setVertexStrider(8);
        mesh.setMaxInfluences(0);
        mesh.setupInterleavedMesh(shaderProgram);
        List<Mesh> mesheList= new ArrayList<>();
        mesheList.add(mesh);
        setMesh(mesheList);
    }

    public void updateCenterRadiusPlane(){
        currentCenter.set(center);
        currentCenter.transform(getModelMatrix());
        currentV0 = vertices[0].clone();
        currentV0.transform(getModelMatrix());
        radius = currentV0.getSub(currentCenter).length();
        //create normal matrix
        System.arraycopy(getModelMatrix(), 0, normalMatrix, 0, getModelMatrix().length);
        Matrix.translateM(normalMatrix, 0, 0, 0,0);
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0);
        Matrix.invertM(normalMatrix, 0, normalMatrix, 0);
        currentPlane.update(getModelMatrix(), normalMatrix);
    }

    private void selectTest(LineEquation line){
        Vector3f intersection = currentPlane.intersection(line);
        System.out.println(TAG+" intersection "+intersection);
        System.out.println(TAG+" plane: "+currentPlane);

        float distance;
        if(intersection == null){
            //this method is totally complete to this task!!!!
            //select test based to radius
            distance = line.distance(currentCenter);
        }else{
            distance = intersection.getSub(currentCenter).length();
        }

        System.out.println(TAG+" center "+currentCenter+" distance "+distance+" radius "+radius+" plane normal"+currentPlane.getNormal());

        if( distance <= radius){
            setSelected(1);
            System.out.println(TAG+" select test: 1");
        }else {
            setSelected(0);
            System.out.println(TAG+" select test: 0");
        }
    }

    @Override
    public void update(Timer timer,
                       Transformation transformation,
                       MousePicker mousePicker,
                       Input input, Map<String,
                        GameObject> gameObjectMap) {
        if(input.isTouch()){
            selectTest(mousePicker.getLine());
            if(isSelected() > 0){
                setTouched(true);
                setTouchCounter(getTouchCounter()+1);
            }
        }

        updateBonds(gameObjectMap);
        updateTranslation(timer);
        updateRotation(timer);
        updateScale();
        if(isUpdateModelMatrix()) {
            updateModelMatrix(transformation);
            updateCenterRadiusPlane();
        }
    }

    @Override
    public void render(Scene scene, Transformation transformation){
        for(Mesh mesh: this.getMeshList()) {
            if(mesh == null) return; //do nothing

            int fromIndex = 0;
            //material index
            int mIndex = 0;

            for(Material material: mesh.getMaterials()) {

                ShaderProgram shaderProgram = scene.getShaderPrograms()
                        .get(getShaderProgramId());

                material.setupSampler2d(mIndex);

                if (shaderProgram != null) {
                    shaderProgram.passIntUniforms(
                            BUTTON_INT_UNIFORMS, material.getSample2Did(), new int[]{isSelected()});
                    shaderProgram.passFloatUniforms(BUTTON_FLOAT_UNIFORMS, getMvpMatrix());
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
