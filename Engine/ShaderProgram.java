package com.example.openglexemple.Engine;


import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glGetShaderiv;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.example.openglexemple.Engine.Loader;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderProgram {

    private static final String TAG = "SHADER_PROGRAM";
    private final int programHandle;
    private final Map<String, Integer> intUniformHandles = new HashMap<>();
    private final Map<String, Integer> floatUniformHandles = new HashMap<>();;
    private final Map<String, Integer> attributeHandles = new HashMap<>();;
    private final Map<String, Integer> mat4ArrUniformHandles = new HashMap<>();;
    private final String vertexName;
    private final String fragmentName;
    private static final FloatBuffer matrixBuffer = Utils.createFloatBuffer(16);

    public ShaderProgram(final String vertexName, final String fragmentName, final String[] attributes, Loader loader){
        this.vertexName = vertexName;
        this.fragmentName = fragmentName;

        String vertexString = loader.loadShader(vertexName);
        String fragmentString = loader.loadShader(fragmentName);

        programHandle = createShaderProgram(vertexString, fragmentString, attributes);
    }
    /**
     * Helper function to compile a shader.
     *
     * @return An OpenGL handle to the shader.
     */
    public int createShaderProgram(final String vertexString, final String fragmentString, final String[] attributes)
    {
        final int vertexShaderHandle   = compileShader(GL_VERTEX_SHADER, vertexString);
        final int fragmentShaderHandle = compileShader(GL_FRAGMENT_SHADER, fragmentString);
        int programHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                attributes);
        Log.wtf(TAG, vertexName + " program id "+programHandle);
        return programHandle;
    }

    private int  compileShader(final int shaderType, final String shaderSource)
    {
        int shaderHandle = GLES30.glCreateShader(shaderType);

        if (shaderHandle != 0)
        {
            // Pass in the shader source.
            GLES30.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES30.glCompileShader(shaderHandle);

            // Get the compilation status.
            int[] compileStatus = new int[1];
            glGetShaderiv(shaderHandle, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                if(shaderType == GL_VERTEX_SHADER){
                    Log.d(TAG, "Load "+vertexName+" failed: "+GLES30.glGetShaderInfoLog(shaderHandle));

                }else{
                    Log.d(TAG, "Load "+fragmentName+" failed: "+GLES30.glGetShaderInfoLog(shaderHandle));
                }

                GLES30.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0)
        {
            if(shaderType == GL_VERTEX_SHADER){
                Log.d(TAG, "Load "+vertexName+"  failed. Creation");
            }else{
                Log.d(TAG, "Load "+fragmentName+"  failed. Creation");
            }

            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int createAndLinkProgram(int vertexShaderHandle, int fragmentShaderHandle, String[] attributes)
    {
        int programHandle = GLES30.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES30.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES30.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null)
            {
                for (int i = 0; i < attributes.length; i++) {
                    GLES30.glBindAttribLocation(programHandle, i, attributes[i]);
                    attributeHandles.put(attributes[i],i);
                }
            }
            // Link the two shaders together into a program.
            GLES30.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(programHandle, GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                Log.e(TAG, "Error compiling program: " + GLES30.glGetProgramInfoLog(programHandle));
                GLES30.glDeleteProgram(programHandle);
                programHandle = 0;
            }

            //test erase after

            for(String attribName : attributes){
                int handle = GLES30.glGetAttribLocation(programHandle, attribName);
                System.out.println(TAG+" ATTRIBUTE :"+attribName +", INDEX: "+handle);
            }

        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    public int getProgramHandle(){
        return this.programHandle;
    }

    public void enableVertexAttribArray(String attributeName, int size, int stride, int pointer, int type){
        Integer handleInteger = attributeHandles.get(attributeName);

        int handle = -1;
        if(handleInteger!=null){
            handle = handleInteger;
        }else{
            System.out.println(TAG+" attribute "+attributeName+ "is null");
        }

        IntBuffer max = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS,max);

        GLES30.glEnableVertexAttribArray(handle);

        if(type == GLES20.GL_FLOAT) {
            GLES30.glVertexAttribPointer(handle, size, type, false,
                    stride, pointer);
            System.out.println(TAG+" set vertexAttribPointer of "+handle);
        }
        if(type == GLES20.GL_INT){
            GLES30.glVertexAttribIPointer(handle, size, type, stride, pointer);
        }
        //the new way
        //GLES31.glVertexAttribFormat();
    }

    public void setIntUniformHandles(final String[] intUniforms) {
        for (String uniformName : intUniforms) {
            int handle = glGetUniformLocation(programHandle, uniformName);
            intUniformHandles.put(uniformName, handle);
        }

    }
    public void setFloatUniformHandles(final String[] floatUniforms) {
        for (String uniformName : floatUniforms) {
            int handle = glGetUniformLocation(programHandle, uniformName);
            floatUniformHandles.put(uniformName, handle);
        }
    }

    public void setAttributeHandles(final String[] attributes){
        for(String attribName : attributes){
            int handle = GLES30.glGetAttribLocation(programHandle, attribName);
            attributeHandles.put(attribName,handle);
        }
    }

    //store uniform mat4 arrays
    public void setMat4ArrUniformHandles(final String[] mat4ArrUniforms, int size){
        for(String uniformArrayName : mat4ArrUniforms){
            for(int i=0; i < size; i++) {
                String uniformName = uniformArrayName+"["+i+"]";
                int location = glGetUniformLocation(programHandle, uniformName);
                mat4ArrUniformHandles.put(uniformName, location);
            }
        }
    }

    public void setMat4ArrUniformHandles(final String[] mat4ArrUniforms) {
        for(String uniformName : mat4ArrUniforms) {
            int location = glGetUniformLocation(programHandle, uniformName);
            mat4ArrUniformHandles.put(uniformName, location);
        }
    }

    public void passAttrib3f(String name, float[] attribute){
        Integer location = attributeHandles.get(name);
        if(location == null){
            location = -1;
        }
        GLES30.glVertexAttrib3f(location, attribute[0], attribute[1], attribute[2]);
    }

    public void passAttrib3f(String name, Vector3f attribute){
        Integer location = attributeHandles.get(name);
        if(location == null){
            location = -1;
        }
        GLES30.glVertexAttrib3f(location, attribute.x, attribute.y, attribute.z);
    }

    // flatUniforms and data must be in same order
    public void passFloatUniforms(final String[] floatUniforms,  final float[]... data){
        for(int i = 0; i < floatUniforms.length;i++){
            Integer location = floatUniformHandles.get(floatUniforms[i]);
            int loc = -1;
            if(location != null){
                loc = location;
            }
            passFloatUniform(loc, data[i]);
        }
    }
    // intUniforms and data must be in same order and length
    public void passIntUniforms(final String[] intUniforms,  final int[]... data){
        for(int i = 0; i<intUniforms.length;i++){
            Integer location = intUniformHandles.get(intUniforms[i]);
            int loc = -1;
            if(location != null){
                loc = location;
            }
            passIntUniform(loc , data[i]);
        }
    }

    private void passIntUniform(final int handle, final int[] data){
        if(data.length == 1){
            GLES30.glUniform1i(handle, data[0]);
        }
        if(data.length == 2){
            GLES30.glUniform2i(handle,data[0], data[1]);
        }
    }

    private void passFloatUniform(int location, final float[] data){
        if(data.length == 1){
            GLES30.glUniform1f(location, data[0]);
        }
        if(data.length == 2){
            GLES30.glUniform2f(location, data[0], data[1]);
        }
        if(data.length == 3){
            GLES30.glUniform3f(location, data[0], data[1], data[2]);
        }
        if(data.length == 4){
            GLES30.glUniform4f(location, data[0], data[1], data[2], data[3]);
        }
        if(data.length == 16){
            GLES30.glUniformMatrix4fv(location, 1, false, data, 0);
        }
    }

    public void passUniformMat4Array(String uniformName, float[][] data){

        //System.out.print(TAG+" passUniformMat4Array jointsTransforms: ");
        for(int i =0; i< data.length; i++) {
            String uniformArrayName = uniformName+"["+i+"]";
            Integer location = mat4ArrUniformHandles.get(uniformArrayName);
            int loc = -1;
            if(location != null){
                loc = location;
            }
            //System.out.println(TAG+" pass jointTransform loc "+loc+" matrix ["+i+"] "+Arrays.toString(data[i]));
            //System.out.print(Arrays.toString(data[i]));

            //matrixBuffer.put(data[i]);
            //matrixBuffer.flip();

            GLES30.glUniformMatrix4fv(loc,1, false, data[i], 0);
            //GLES20.glUniformMatrix4fv(loc, 1, false, matrixBuffer);
            //matrixBuffer.rewind();
        }
        //System.out.println();
    }

    public void passUniformMat4Array(String uniformName, FloatBuffer data){
        Integer location = mat4ArrUniformHandles.get(uniformName);
        int loc = -1;
        if(location != null){
            loc = location;
        }

        GLES30.glUniformMatrix4fv(loc, data.capacity(), false,data);
        data.rewind();
    }

    public void passUniformMat4(int location , int size, final FloatBuffer data){
        GLES30.glUniformMatrix4fv(location, size, false, data);
    }
    public void passUniformFloat3(int location, final float[] data){
        GLES30.glUniform3f(location, data[0], data[1], data[2]);
    }
    public void passUniformFloat2(int handle, final float[] data){
        GLES30.glUniform2f(handle, data[0], data[1]);
    }
    public void passUniformFloat1(int handle, final float[] data){
        GLES30.glUniform1f(handle, data[0]);
    }

    public void disableAttributes(){
        for (int handle : attributeHandles.values()) {
            if(handle != -1){
                GLES30.glDisableVertexAttribArray(handle);
            }
        }
        GLES30.glBindVertexArray(0);
    }

    public void start(){
        GLES30.glUseProgram(programHandle);
    }
    public void stop(){
        GLES30.glUseProgram(0);
    }
    public void cleanUp(){
        stop();
        GLES30.glDeleteProgram(programHandle);
    }

}