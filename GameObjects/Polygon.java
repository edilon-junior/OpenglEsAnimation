package com.example.openglexemple.GameObjects;

import static com.example.openglexemple.Constants.POINT_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.POINT_INT_UNIFORMS;
import static com.example.openglexemple.Constants.POSITION_SIZE;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.openglexemple.Colors;
import com.example.openglexemple.Constants;
import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Math.LineEquation;
import com.example.openglexemple.Math.PlaneEquation;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Messenger;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Polygon extends GameObject {

    public static final String TAG = "POLYGON";
    private float radius = 0;
    private final Vector3f v0 = new Vector3f();
    private final Vector3f center = new Vector3f();
    private final Vector3f currentV0 = new Vector3f();
    private final Vector3f currentCenter = new Vector3f();
    private boolean fill = false;
    private Vector3f[] vertices;
    private float[] positions;
    private Vector3f normal;
    private PlaneEquation plane;
    private PlaneEquation currentPlane;
    private ShaderProgram shaderProgram;
    private final float[] normalMatrix = new float[16];
    private Line normalLine;

    float counterTest = 0;
    float oldDtTest = 0;

    /**
     * @param position: define initial position
 * @param sides: define the number of sides
 * @param color: color of lines
 * @param shaderProgram: shader program used by this object
 **/

    public Polygon(Vector3f position, int sides, float[] color, boolean fill, ShaderProgram shaderProgram){
        super();

        this.fill = fill;

        this.shaderProgram = shaderProgram;
        setPosition(position);
        setShaderProgramId(shaderProgram.getProgramHandle());
        setColor(color);

        positions = createPositionArray(sides);
        short[] indices = createIndices(sides);
        normal = createNormal();
        radius = calcRadius();

        createPlane();
        //-------------create and setup mesh ------------
        Mesh polygonMesh = new Mesh(positions, null, normal.toFloat(), null, null, indices);
        polygonMesh.setSemantics(new String[]{"VERTEX"});
        polygonMesh.setupNonInterleavedMesh(shaderProgram);
        List<Mesh> meshes = new ArrayList<>();
        meshes.add(polygonMesh);
        setMesh(meshes);
        setUpdateModelMatrix(true);
    }

    private float calcRadius(){
        return center.getSub(v0).length();
    }

    private float[] createPositionArray(int sides){
        double angle = 2*Math.PI / sides;

        float[] positions = new float[sides * POSITION_SIZE];
        vertices = new Vector3f[sides];
        //length of radius is 1, because this is the standard model size in opengl
        // x = R * cos((i +1)*angle)
        for(int i=0; i < sides; i++){
            float x = (float) (Math.cos(i * angle));
            float y = (float) (Math.sin(i * angle));
            float z = 0;
            positions[i*POSITION_SIZE] = x;
            positions[i*POSITION_SIZE + 1] = y;
            positions[i*POSITION_SIZE + 2] = z;
            vertices[i]=new Vector3f(x,y,z);
            center.getAdd(new Vector3f(x,y,z));
        }
        v0.set(vertices[0]);
        center.mul(1.0f /sides);
        currentV0.set(v0);
        currentCenter.set(center);

        return positions;
    }

    public short[] createIndices(int sides){
        short[] indices = new short[sides];
        for(int i=0; i<sides-2;i++){
            indices[i * 3] = 0;
            indices[i * 3 + 1] = (short) (i + 1);
            indices[i * 3 + 2] = (short) (i + 2);
        }
        return indices;
    }

    public Vector3f createNormal(){
        Vector3f p0 = vertices[0].clone();
        Vector3f p1 = vertices[1].clone();
        Vector3f p2 = vertices[2].clone();

        p1.sub(p0);
        p2.sub(p0);

        Vector3f normal = p2.cross(p1);
        normal.normalize();

        return normal;
    }

    private void createPlane(){
        plane = new PlaneEquation(currentV0, normal);
        currentPlane = new PlaneEquation(plane);
    }

    public void createNormalLine(){
        Vector3f[] normalLineVertices = new Vector3f[]{center, normal.getMul(1.5f) };
        normalLine = new Line(normalLineVertices, Colors.RED, shaderProgram);
        normalLine.setName(getName()+"-normalLine");
        Messenger[] bonds = new Messenger[]{
                new Messenger(Constants.INFO_MODEL_MATRIX),null,
                new Messenger(Constants.INFO_MODEL_MATRIX),null};
        normalLine.setParentBonds(getName(), bonds);
    }

    public Line getNormalLine(){
        return normalLine;
    }

    private void selectTest(LineEquation line){
        Vector3f intersection = currentPlane.intersection(line);
        float distance = intersection.getSub(currentCenter).length();
         if( distance <= radius){
           setSelected(1);
        }else {
            setSelected(0);
        }
    }

    public void updateCenterRadiusPlane(){
        currentCenter.set(center);
        currentCenter.transform(getModelMatrix());
        currentV0.set(vertices[0].clone());
        currentV0.transform(getModelMatrix());
        radius = currentV0.getSub(currentCenter).length();
        //create normal matrix
        System.arraycopy(getModelMatrix(), 0, normalMatrix, 0, getModelMatrix().length);
        Matrix.translateM(normalMatrix, 0, 0, 0,0);
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0);
        Matrix.invertM(normalMatrix, 0, normalMatrix, 0);
        plane.update(getModelMatrix(), normalMatrix);
    }

    @Override
    public void update(Timer timer,
                       Transformation transformation,
                       MousePicker mousePicker,
                       Input input,
                       Map<String, GameObject> gameObjectMap) {
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
    public void render(ShaderProgram shaderProgram, Transformation transformation){
        //mvpMatrix = transformation.getMVPMatrix(getModelMatrix());
        shaderProgram.passFloatUniforms(POINT_FLOAT_UNIFORMS, getMvpMatrix(), getColor(), getPointSize());
        shaderProgram.passIntUniforms(POINT_INT_UNIFORMS, new int[]{isSelected()} );
        for(Mesh mesh: getMeshList()){
            if(fill) {
                mesh.render(GLES30.GL_TRIANGLES, mesh.getIndices().length, 0);
            }else {
                mesh.render(GLES30.GL_LINE_LOOP, 0, 0);
            }
        }
    }
}
