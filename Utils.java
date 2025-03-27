package com.example.openglexemple;

import static android.opengl.GLES20.GL_TEXTURE_2D;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.openglexemple.GameObjects.GameObject;
import com.example.openglexemple.GraphicObjects.Mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final String TAG = "UTILS";

    public static void printMatrix(float[] mat){
        String result = "";
        result += mat[0]+" "+mat[4]+" "+mat[8]+" "+mat[12]+"\n";
        result += mat[1]+" "+mat[5]+" "+mat[9]+" "+mat[13]+"\n";
        result += mat[2]+" "+mat[6]+" "+mat[10]+" "+mat[14]+"\n";
        result += mat[3]+" "+mat[7]+" "+mat[11]+" "+mat[15]+"\n";
        System.out.print(result);
    }
    public static void printFloatArray(float[] floatArray){
        String result = "";
        for(float i : floatArray){
            result = result +i+", ";
        }
        result = result + "\n";
        System.out.print(result);
    }
    public static void printFloatArray(String title, float[] floatArray){
        String result = "title: ";
        for(float i : floatArray){
            result = result +i+", ";
        }
        result = result + "\n";
        System.out.print(result);
    }

    public static void printIntArray(String title, int[] intArray){
        String result = "title: ";
        for(int i : intArray){
            result = result + i + ", ";
        }
        result = result + "\n";
        System.out.print(result);
    }

    public static List<String> readAllLines(String modelName, Context context) throws Exception {
        List<String> list = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("models/" + modelName), StandardCharsets.UTF_8));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int[] intListToArray(List<Integer> list){
        int[] result = new int[list.size()];
        for(int i=0; i< list.size();i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static int[] intListToArray(ArrayList<Integer> list){
        int[] result = new int[list.size()];
        for(int i=0; i< list.size();i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static float[] floatListToArray(ArrayList<Float> list){
        float[] result = new float[list.size()];
        for(int i=0; i< list.size();i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static float[] floatListToArray(List<float[]> list, int stride){
        int innerLength = list.get(0).length;
        float[] result = new float[list.size() * innerLength];
        for(int i=0; i< list.size();i++){
            for(int j=0; j<stride; j++) {
                result[i * innerLength + j] = list.get(i)[j];
            }
        }
        return result;
    }

    public static void printVertexArray(float[] va){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <  va.length / 8; i++) {
            sb.append("v"+i+" ");
            sb.append( va[i*8]  +", "+va[i*8+1]+", "+va[i*8+2]+"\t\t"
                      +va[i*8+3]+", "+va[i*8+4]+"\t\t"
                      +va[i*8+5]+", "+va[i*8+6]+", "+va[i*8+7]+"\n");
        }
        System.out.println(sb.toString());
    }

    public static void meshMatcher(ArrayList<GameObject> goList, ArrayList<Mesh> meshList){
        for(int i=0; i < goList.size();i++){

        }
    }

    public static Short parseShort(String string){
        if(string.isEmpty()){
            return -1;
        }
        if(string == null){
            return -1;
        }
        return Short.parseShort(string);
    }

    public static Integer parseInt(String string){
        if(string.isEmpty()){
            return -1;
        }
        if(string == null){
            return -1;
        }
        return Integer.parseInt(string);
    }

    public static void printIntList(List<Integer[]> list){
        String result = "";

        for(Integer[] f : list){
            result = result +"{"+f[0]+", "+f[1]+", "+f[2]+"}";
        }
        result = result + "\n";
        System.out.print(result);
    }
    public static void printFloatList(List<float[]> list) {
        StringBuilder result = new StringBuilder();

        result.append("{");
        for (float[] f : list) {
            for (int i = 0; i < f.length; i++) {
                result.append(f[i]);
                if (i < f.length - 1) {
                    result.append(", ");
                }
            }
            result.append(" ");
        }
        result.append("}");
        result.append("\n");
        System.out.print(result);
    }

    public static float[] stringToFloatArray(String string){
        String[] stringArray = string.split("\\s+");
        float[] result = new float[stringArray.length];
        for(int i=0; i<stringArray.length; i++){
            result[i] = Float.parseFloat(stringArray[i]);
        }
        return result;
    }

    public static int[] stringToIntArray(String string){
        String[] stringArray = string.split("\\s+");
        int[] result = new int[stringArray.length];
        for(int i=0; i<stringArray.length; i++){
            result[i] = Integer.parseInt(stringArray[i]);
        }
        return result;
    }

    public static short[] stringToShortArray(String string){
        String[] stringArray = string.split("\\s+");
        short[] result = new short[stringArray.length];
        for(int i=0; i<stringArray.length; i++){
            result[i] = Short.parseShort(stringArray[i]);
        }
        return result;
    }

    /**
     * create a texture with bitmap of 1 x 1
     * @return texture value id
     */
    public static int[] createFakeTexture(){
        final int[] textureHandle = new int[]{-1};
        final Bitmap bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
        GLES20.glGenTextures(1, textureHandle, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, textureHandle[0]);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        return textureHandle;
    }

    public static FloatBuffer createFloatBuffer(int size){
        return ByteBuffer.allocateDirect(size * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static void fillArrayWithIdentityMatrix(float[][] array){
        for(int i=0; i < array.length; i++){
            float[] transform = new float[16];
            Matrix.setIdentityM(transform,0);
            array[i] = transform;
        }
    }

    public static void fillArrayWithIdentityMatrix(FloatBuffer array){
        for(int i=0; i < array.capacity() / 16; i++){
            float[] transform = new float[16];
            Matrix.setIdentityM(transform,0);
            array.put(transform);
        }
        array.rewind();
    }

    public static void fillArrayWithIdentityMatrix(FloatBuffer[] array){
        for(int i=0; i< array.length; i++){
            float[] transform = new float[16];
            Matrix.setIdentityM(transform,0);
            array[i] = Utils.createFloatBuffer(16).put(transform);
        }
    }

    public static float[] getRotation(float[] matrix){
        float[] rot = new float[matrix.length];
        rot[0] = matrix[0];
        rot[1] = matrix[1];
        rot[2] = matrix[2];
        rot[3] = 0;
        rot[4] = matrix[4];
        rot[5] = matrix[5];
        rot[6] = matrix[6];
        rot[7] = 0;
        rot[8] = matrix[8];
        rot[9] = matrix[9];
        rot[10] = matrix[10];
        rot[11] = 0;
        rot[12] = 0;
        rot[13] = 0;
        rot[14] = 0;
        rot[15] = 1;
        return rot;
    }

}
