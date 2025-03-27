package com.example.openglexemple.GraphicObjects;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_LINE_LOOP;
import static android.opengl.GLES30.GL_POINTS;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.GL_UNSIGNED_SHORT;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glBufferData;
import static android.opengl.GLES30.glDrawElements;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGenVertexArrays;
import static com.example.openglexemple.Constants.ATTRIBUTE_COLOR;
import static com.example.openglexemple.Constants.ATTRIBUTE_JOINTS_ID;
import static com.example.openglexemple.Constants.ATTRIBUTE_NORMAL;
import static com.example.openglexemple.Constants.ATTRIBUTE_POSITION;
import static com.example.openglexemple.Constants.ATTRIBUTE_TEXTURE_COORDINATE;
import static com.example.openglexemple.Constants.ATTRIBUTE_WEIGHTS;
import static com.example.openglexemple.Constants.BYTES_PER_FLOAT;
import static com.example.openglexemple.Constants.BYTES_PER_SHORT;
import static com.example.openglexemple.Constants.COLOR_SIZE;
import static com.example.openglexemple.Constants.NORMAL_SIZE;
import static com.example.openglexemple.Constants.POSITION_SIZE;
import static com.example.openglexemple.Constants.TEXTURE_COORDINATE_SIZE;

import android.opengl.GLES30;
import android.util.Log;

import com.example.openglexemple.Engine.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Mesh {

    private static final String TAG = "MESH";

    private int vertexStrider = 0;
    private int vboLength = 0;
    private int iboLength = 0;
    final int[] vao = new int[1];
    private int[] vbo;
    private final int[] ibo = new int[1];
    protected float[] vertices;
    private float[] positions;
    private float[] textures;
    private float[] normals;
    private float[] weights;
    private float[] jointsId;
    private int maxInfluences;
    private float[] colors;
    private short[] indices;
    private FloatBuffer vertexBuffer;
    private FloatBuffer positionBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;
    private String modelName;
    private String name;
    private String[] semantics;
    private boolean homogeneous = false;
    private String materialId;
    Material[] materials;

    public Mesh(float[] positions, float[] textures, float[] normals, float[] colors, float[] vertices, short[] indices) {
        this.positions = positions;
        this.textures = textures;
        this.normals = normals;
        this.colors = colors;
        this.vertices = vertices;
        this.indices = indices;
        this.maxInfluences = 0;
    }

    public Mesh(float[] vertices, short[] indices, int stride, String[] semantics){
        this.positions = null;
        this.textures = null;
        this.normals = null;
        this.colors = null;
        this.vertices = vertices;
        this.indices = indices;
        this.vertexStrider = stride;
        this.semantics = semantics;
        this.maxInfluences = 0;
    }

    public Material[] getMaterials(){
        return this.materials;
    }

    public void setMaterials(Material[] material){
        this.materials = material;
    }

    public void setupInterleavedMesh(ShaderProgram shaderProgram){
        createBuffers();
        vbo = new int[vboLength];
        createVBO(vboLength);
        createIBO(iboLength);
        setupBufferData();
        createVAO();
        bindVAO();
        setupInterleavedAttributes(shaderProgram, maxInfluences, vertexStrider, semantics);
        //unbindVBO(1);
        unbindVAO();
    }

    public void setupNonInterleavedMesh(ShaderProgram shaderProgram){
        createNonInterleavedBuffers();
        vbo = new int[vboLength];
        createVBO(vboLength);
        createIBO(iboLength);
        setNoInterleavedBufferData();
        createVAO();
        bindVAO();
        setupNoInterleavedAttributes(shaderProgram);
        //unbindVBO(3);
        unbindVAO();
    }
    /**
     * Create interleaved buffers
     */
    public void createBuffers() {
        if(vertices != null){
            vertexBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(vertices).position(0);
            vboLength++;
        }

        if(indices != null){
            indexBuffer = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBuffer.put(indices).position(0);
            iboLength++;
        }
    }

    public void createNonInterleavedBuffers(){
        // Initialize the buffers.
        for(String s : semantics) {
            if (s.equalsIgnoreCase("VERTEX")) {
                positionBuffer = ByteBuffer.allocateDirect(positions.length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                positionBuffer.put(positions).position(0);
                vboLength++;
            }
            if (s.equalsIgnoreCase("COLOR")) {
                colorBuffer = ByteBuffer.allocateDirect(colors.length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                colorBuffer.put(colors).position(0);
                vboLength++;
            }
            if (s.equalsIgnoreCase("NORMAL")) {
                normalBuffer = ByteBuffer.allocateDirect(normals.length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                normalBuffer.put(normals).position(0);
                vboLength++;
            }
            if (s.equalsIgnoreCase("TEXCOORD")) {
                textureBuffer = ByteBuffer.allocateDirect(textures.length * BYTES_PER_FLOAT)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                textureBuffer.put(textures).position(0);
                vboLength++;
            }
        }
        if(indices != null){
            indexBuffer = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBuffer.put(indices).position(0);
            iboLength++;
        }
    }
    public void createVBO(int n) {
        glGenBuffers(n, vbo, 0);
    }
    public void createIBO(int n){
        glGenBuffers(n, ibo, 0);
    }
    public void createVAO() {
        glGenVertexArrays(1, vao, 0);
    }

    /**
     * setup interleaved data
     */
    public void setupBufferData(){
        if (vbo[0] > 0 && ibo[0] > 0) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT,
                    indexBuffer, GL_STATIC_DRAW);
        } else {
            Log.e(TAG, "VBO and IBO are not generated");
        }
    }

    public void setNoInterleavedBufferData(){
        for(int i=0; i<semantics.length; i++) {
            if (semantics[i].equalsIgnoreCase("VERTEX")) {
                //set position in vbo
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                glBufferData(GL_ARRAY_BUFFER, positionBuffer.capacity() * BYTES_PER_FLOAT,
                        positionBuffer, GL_STATIC_DRAW);
            }
            if(semantics[i].equalsIgnoreCase("COLOR")){
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                glBufferData(GL_ARRAY_BUFFER, colorBuffer.capacity() * BYTES_PER_FLOAT,
                        colorBuffer, GL_STATIC_DRAW);
            }
            if (semantics[i].equalsIgnoreCase("TEXCOORD")) {
                //texture coordinate vbo
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                glBufferData(GL_ARRAY_BUFFER, textureBuffer.capacity() * BYTES_PER_FLOAT,
                        textureBuffer, GL_STATIC_DRAW);
            }
            if (semantics[i].equalsIgnoreCase("NORMAL")) {
                //normal vbo
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                glBufferData(GL_ARRAY_BUFFER, normalBuffer.capacity() * BYTES_PER_FLOAT,
                        normalBuffer, GL_STATIC_DRAW);
            }
        }
        if(indices != null){
            //ibo
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT,
                    indexBuffer, GL_STATIC_DRAW);
        }
    }
    /**
     *
     * @param shaderProgram shader program to this mesh
     * @param mi maximum joint interaction
     * @param stride vertex attribute stride
     * @param semantics attributes names
     */
    public void setupInterleavedAttributes(ShaderProgram shaderProgram,int mi, int stride, String[] semantics){
        //bind vbo id
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        stride *= BYTES_PER_FLOAT;
        int pointer = 0;
        int mod = 0;
        if(homogeneous){
            mod += 1;
        }

        for (String semantic : semantics) {
            if(semantic != null) {
                if (semantic.equalsIgnoreCase("VERTEX")) {
                    shaderProgram.enableVertexAttribArray(ATTRIBUTE_POSITION, POSITION_SIZE, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);
                    pointer += POSITION_SIZE + mod;
                }
                if (semantic.equalsIgnoreCase("NORMAL")) {
                    shaderProgram.enableVertexAttribArray(ATTRIBUTE_NORMAL, NORMAL_SIZE, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);
                    pointer += NORMAL_SIZE + mod;
                }
                if (semantic.equalsIgnoreCase("TEXCOORD")) {
                    shaderProgram.enableVertexAttribArray(ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_SIZE, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);
                    pointer += TEXTURE_COORDINATE_SIZE;
                }
                if (semantic.equalsIgnoreCase("COLOR")) {
                    shaderProgram.enableVertexAttribArray(ATTRIBUTE_COLOR, COLOR_SIZE, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);
                    pointer += COLOR_SIZE;
                }
            }
        }

        if(mi > 0) {
            shaderProgram.enableVertexAttribArray(ATTRIBUTE_JOINTS_ID, mi, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);

            pointer += mi;
            shaderProgram.enableVertexAttribArray(ATTRIBUTE_WEIGHTS, mi, stride, pointer * BYTES_PER_FLOAT, GL_FLOAT);
        }
    }
    public void setupNoInterleavedAttributes(ShaderProgram shaderProgram){

        for(int i=0; i<semantics.length;i++) {
            if (semantics[i].equalsIgnoreCase("VERTEX")){
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                shaderProgram.enableVertexAttribArray(ATTRIBUTE_POSITION, POSITION_SIZE, 0, 0, GL_FLOAT);
            }
            if(semantics[i].equalsIgnoreCase("COLOR")){
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                shaderProgram.enableVertexAttribArray(ATTRIBUTE_COLOR, COLOR_SIZE, 0,0, GL_FLOAT);
            }
            if (semantics[i].equalsIgnoreCase("TEXCOORD")) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                shaderProgram.enableVertexAttribArray(ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_SIZE, 0, 0, GL_FLOAT);
            }
            if (semantics[i].equalsIgnoreCase("NORMAL")) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                glBindBuffer(GL_ARRAY_BUFFER, vbo[i]);
                shaderProgram.enableVertexAttribArray(ATTRIBUTE_NORMAL, NORMAL_SIZE, 0, 0, GL_FLOAT);
            }
        }
    }
    public void bindVAO(){
        glBindVertexArray(vao[0]);
    }

    public void unbindVBO(int i){
        glBindBuffer(GL_ARRAY_BUFFER, i);
    }

    public void unbindVAO(){
        glBindVertexArray ( 0 );
    }

    public void render(int type, int count, int offset){
        // Bind the VAO
        bindVAO();

        // Draw
        switch (type){
            case GL_TRIANGLES:
                glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, offset);
                break;
            case GL_POINTS:
                GLES30.glDrawArrays(GL_POINTS, 0, 1);
                break;
            case GL_LINE_LOOP:
                GLES30.glDrawArrays(GL_LINE_LOOP, 0, positions.length / POSITION_SIZE);
                break;
        }

        unbindVAO();
    }
    public void setMaxInfluences(int maxInfluences) {
        this.maxInfluences = maxInfluences;
    }
    public int getVertexStrider() {
        return vertexStrider;
    }
    public void setVertexStrider(int vertexStrider) {
        this.vertexStrider = vertexStrider;
    }
    public void setPositions(float[] positions){
        this.positions = positions;
    }
    public float[] getPositions(){
        return positions;
    }
    public void setNormals(float[] normals){
        this.normals = normals;
    }
    public float[] getNormals(){
        return normals;
    }
    public void setColors(float[] colors){
        this.colors = colors;
    }
    public float[] getColors() {
        return colors;
    }
    public float[] getVertices(){
        return vertices;
    }
    public short[] getIndices(){
        return indices;
    }
    public void setModelName(String modelName){
        this.modelName = modelName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setSemantics(String[] semantics) {
        this.semantics = semantics;
    }

    public String[] getSemantics() {
        return this.semantics;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialId(){
        return materialId;
    }
    public void setHomogeneous(boolean homogeneous){
        this.homogeneous = homogeneous;
    }
    public void delete(){
        GLES30.glDeleteVertexArrays(1, vao, 0);
        GLES30.glDeleteBuffers(vboLength, vbo, 0);
    }
}
