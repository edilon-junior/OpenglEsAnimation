package com.example.openglexemple;

import android.content.res.AssetManager;

import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PolygonParser {

    public static final String TAG = "POLYGON_PARSER";
    private AssetManager assetManager;
    private String modelName = "";

    public PolygonParser(AssetManager assetManager, String modelName){
        this.assetManager = assetManager;
        this.modelName = modelName;
    }

    public Mesh getMesh(){
        System.out.println(TAG+" try to load solidmodel: "+modelName);
        float[] vertices = new float[0];
        short[] indices = new short[0];
        float[] positions = new float[0];
        float[] normals = new float[0];
        ArrayList<String> semantics = new ArrayList<>();
        Material material = new Material();
        String textureName = "";
        String materialLib = "";
        int faceCounter = 0;
        int verticesByFace = 0;
        int numberOfTriangles = 0;
        int lineCounter = 0;
        int verticesSize = 0;
        int facesSize = 0;
        int stride = 0;

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(assetManager.open(
                            "models/" + modelName + "/" + modelName + ".ply"),
                            StandardCharsets.UTF_8));
            String line;
            boolean endHeader = false;
            try{
                while ((line = bufferedReader.readLine()) != null) {
                    String[] ls = line.split("\\s+");

                    if (endHeader == false) {
                        switch (ls[0]) {
                            case "comment":{
                                if (ls[1].equalsIgnoreCase("TextureFile")){
                                    textureName = ls[2];
                                    material.setTextureFileName(textureName);
                                }
                            }
                            case "property": {
                                if (ls[2].equalsIgnoreCase("x")) {
                                    stride++;
                                    semantics.add("VERTEX");
                                }
                                if (ls[2].equalsIgnoreCase("y")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("z")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("nx")) {
                                    stride++;
                                    semantics.add("NORMAL");
                                }
                                if (ls[2].equalsIgnoreCase("ny")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("nz")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("red")) {
                                    semantics.add("COLOR");
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("green")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("blue")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("alpha")) {
                                    stride++;
                                }
                                if (ls[2].equalsIgnoreCase("s")) {
                                    stride++;
                                    semantics.add("TEXCOORD");
                                }
                                if (ls[2].equalsIgnoreCase("t")) {
                                    stride++;
                                }
                                if (ls[1].equalsIgnoreCase("material_index")) {
                                    material.setIndex(Integer.parseInt(ls[2]));
                                }
                                if (ls[1].equalsIgnoreCase("ambient_red")) {
                                    material.getAmbient()[0] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("ambient_green")) {
                                    material.getAmbient()[1] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("ambient_blue")) {
                                    material.getAmbient()[2] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("ambient_coeff")) {
                                    System.out.println("nothing");
                                }
                                if (ls[1].equalsIgnoreCase("diffuse_red")) {
                                    material.getDiffuse()[0] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("diffuse_green")) {
                                    material.getDiffuse()[1] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("diffuse_blue")) {
                                    material.getDiffuse()[2] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("diffuse_coeff")) {
                                    System.out.println("nothing");
                                }
                                if (ls[1].equalsIgnoreCase("specular_red")) {
                                    material.getSpecular()[0] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("specular_green")) {
                                    material.getSpecular()[1] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("specular_bue")) {
                                    material.getSpecular()[2] = Float.parseFloat(ls[2]);
                                }
                                if (ls[1].equalsIgnoreCase("specular_coeff")) {
                                    System.out.println("nothing");
                                }
                                if (ls[1].equalsIgnoreCase("specular_power")) {
                                    System.out.println("nothing");
                                }
                                break;
                            }
                            case "element": {
                                String secondParam = ls[1];
                                if (secondParam.equalsIgnoreCase("vertex")) {
                                    verticesSize = Integer.parseInt(ls[2]);
                                    positions = new float[verticesSize * Constants.POSITION_SIZE];
                                }
                                if (secondParam.equalsIgnoreCase("face")) {
                                    facesSize = Integer.parseInt(ls[2]);
                                }
                                break;
                            }
                            case "end_header": {
                                endHeader = true;
                                vertices = new float[verticesSize * stride];
                                positions = new float[verticesSize * 3];
                                normals = new float[verticesSize * 3];
                                break;
                            }
                        }
                    } else {
                        //get vertices
                        if (lineCounter < verticesSize && indices.length == 0) {
                            for (int i = 0; i < stride; i++) {
                                if (i < 3) {
                                    vertices[lineCounter * stride + i] = Float.parseFloat(ls[i]);
                                    positions[lineCounter * Constants.POSITION_SIZE + i] =
                                            Float.parseFloat(ls[i]);
                                }
                                if(i>2 && i<6){
                                    vertices[lineCounter * stride + i] = Float.parseFloat(ls[i]);
                                    normals[lineCounter * 3 + i - 3] = Float.parseFloat(ls[i]);
                                }
                                if(i>5 && i<10){
                                    vertices[lineCounter * stride + 2 + i] = Float.parseFloat(ls[i]);
                                }
                                if(i>9){
                                    vertices[lineCounter * stride - 4 + i ] = Float.parseFloat(ls[i]);
                                }
                            }
                        } else {
                            if (lineCounter == verticesSize) {
                                if (Integer.parseInt(ls[0]) == 3) {
                                    indices = new short[facesSize * Integer.parseInt(ls[0])];
                                }
                                lineCounter = 0;
                            }
                            //get faces
                            verticesByFace = Integer.parseInt(ls[0]);
                            for (int i = 0; i < verticesByFace; i++) {
                                indices[lineCounter * verticesByFace + i] = Short.parseShort(ls[i +1]);
                            }
                        }
                        lineCounter++;
                    }
                }
                numberOfTriangles = indices.length /3;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String fv = Saver.formatVertices(vertices, 12);
        //Saver.writeFileOnInternalStorage(context, modelName, fv);

        String[] semanticsArr = new String[]{"VERTEX", "NORMAL", "TEXCOORD","COLOR" };

        Mesh mesh = new Mesh(vertices, indices, stride, semanticsArr);
        mesh.setPositions(positions);
        mesh.setNormals(normals);
        Material[] materials = new Material[1];
        materials[0] = material;
        mesh.setMaterials(materials);
        mesh.setModelName(modelName);

        return mesh;
    }
}
