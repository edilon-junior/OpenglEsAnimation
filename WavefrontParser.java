package com.example.openglexemple;

import android.content.res.AssetManager;
import android.util.Log;

import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WavefrontParser {

    public static final String TAG = "WAVEFRONT_PARSER";
    AssetManager assetManager;
    String modelName = "";
    String materialLib = "";

    public WavefrontParser(AssetManager assetManager, String modelName){
        this.assetManager = assetManager;
        this.modelName = modelName;
    }

    public ArrayList<Mesh> getMeshLibrary(){
        List<float[]> positionArray = new ArrayList<>();
        List<float[]> textureArray = new ArrayList<>();
        List<float[]> normalArray = new ArrayList<>();
        List<int[]> faceArray = new ArrayList<>();
        String meshName = "";
        String materialName = "";
        int faceCounter = 0;
        String[] semantics = new String[4];
        ArrayList<Mesh> meshLibrary = new ArrayList<>();
        MeshData meshData;
        int pCount = 0, tCount= 0, nCount = 0;
        int pEnd = 0, tEnd= 0, nEnd = 0;
        int meshCount = 0;

        BufferedReader bufferedReader = null;
        try {

            bufferedReader = new BufferedReader(
                    new InputStreamReader(assetManager.open(
                            "models/"+modelName+"/"+ modelName+".obj"),
                            StandardCharsets.UTF_8));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] ls = line.split("\\s+");
                switch (ls[0]) {
                    case "o":
                        meshName = ls[1];
                        meshData = new MeshData(meshName);
                        pEnd = pCount;
                        tEnd = tCount;
                        nEnd = nCount;
                        if(meshCount > 0){
                            meshData.setPositionArray(positionArray);
                            meshData.setNormalArray(normalArray);
                            meshData.setTextureArray(textureArray);
                            meshData.setFaceArray(faceArray);
                            meshData.setMaterialId(materialName);
                            Mesh mesh = createMesh(meshData);
                            mesh.setSemantics(semantics);
                            meshLibrary.add(mesh);
                            faceCounter = 0;
                            positionArray.clear();
                            textureArray.clear();
                            normalArray.clear();
                            faceArray.clear();
                        }
                        meshCount++;
                        break;
                    case "mtllib":
                        materialLib = ls[1];
                        break;
                    case "v":
                        semantics[0] = "VERTEX";
                        positionArray.add(new float[]{
                                Float.parseFloat(ls[1]),
                                Float.parseFloat(ls[2]),
                                Float.parseFloat(ls[3])});
                        pCount++;
                        break;
                    case "vt":
                        semantics[2] = "TEXCOORD";
                        textureArray.add(new float[]{
                                Float.parseFloat(ls[1]),
                                Float.parseFloat(ls[2])});
                        tCount++;
                        break;
                    case "vn":
                        semantics[1] = "NORMAL";
                        normalArray.add(new float[]{
                                Float.parseFloat(ls[1]),
                                Float.parseFloat(ls[2]),
                                Float.parseFloat(ls[3])});
                        nCount++;
                        break;
                    case "usemtl":
                        materialName = ls[1];
                        break;
                    case "f":
                        String[] f1 = ls[1].split("/");
                        faceArray.add(new int[]{
                                Utils.parseInt(f1[0])-pEnd,
                                Utils.parseInt(f1[1])-tEnd,
                                Utils.parseInt(f1[2])-nEnd});
                        String[] f2 = ls[2].split("/");
                        faceArray.add(new int[]{
                                Utils.parseInt(f2[0])-pEnd,
                                Utils.parseInt(f2[1])-tEnd,
                                Utils.parseInt(f2[2])-nEnd});
                        String[] f3 = ls[3].split("/");
                        faceArray.add(new int[]{
                                Utils.parseInt(f3[0])-pEnd,
                                Utils.parseInt(f3[1])-tEnd,
                                Utils.parseInt(f3[2])-nEnd});
                        faceCounter++;
                        break;
                }
            }

            meshData = new MeshData(meshName);
            meshData.setPositionArray(positionArray);
            meshData.setNormalArray(normalArray);
            meshData.setTextureArray(textureArray);
            meshData.setFaceArray(faceArray);
            meshData.setMaterialId(materialName);
            Mesh mesh = createMesh(meshData);
            mesh.setSemantics(semantics);
            meshLibrary.add(mesh);

        } catch (IOException e) {
            Log.d(TAG, "error to read file " + modelName);
            e.printStackTrace();
        }
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.wtf(TAG, "loaded mesh: "+meshName);

        return meshLibrary;
    }

    private Mesh createMesh(MeshData meshData){

        float[] positions = Utils.floatListToArray(meshData.getPositionList(), 3);
        float[] textures  = Utils.floatListToArray(meshData.getTextureList(), 2);
        float[] normals   = Utils.floatListToArray(meshData.getNormalList(), 3);
        short[] indices   = new short[meshData.getFaceList().size()];
        float[] vertices  = new float[meshData.getFaceList().size() * 8];
        int stride = 8;

        for(int i = 0; i < meshData.getFaceList().size(); i++) {
            //position index
            int p = meshData.getFaceList().get(i)[0] - 1;
            //texture index
            int t = meshData.getFaceList().get(i)[1] - 1;
            //normal index
            int n = meshData.getFaceList().get(i)[2] - 1;
            indices[i] = (short) p;

            // obj file vertex is in order p,t,n but dae is p,n,t
            vertices[p * stride    ] = meshData.getPositionList().get(p)[0];
            vertices[p * stride + 1] = meshData.getPositionList().get(p)[1];
            vertices[p * stride + 2] = meshData.getPositionList().get(p)[2];
            vertices[p * stride + 3] = meshData.getNormalList().get(n)[0];
            vertices[p * stride + 4] = meshData.getNormalList().get(n)[1];
            vertices[p * stride + 5] = meshData.getNormalList().get(n)[2];
            vertices[p * stride + 6] = meshData.getTextureList().get(t)[0];
            vertices[p * stride + 7] = meshData.getTextureList().get(t)[1];
        }
        //String fv = Saver.formatVertices(vertices, stride);
        //Saver.writeFileOnInternalStorage(context, modelName,fv);
        //--------------------------------------------------------------
        Mesh mesh = new Mesh(positions, textures, normals, null, vertices, indices);
        mesh.setVertexStrider(stride);
        mesh.setName(meshData.getName());
        mesh.setMaterialId(meshData.getMaterialId());
        mesh.setMaxInfluences(0);

        return mesh;
    }
    public Map<String, Material> getMaterialLibrary() {

        Map<String, Material> materialLibrary = new HashMap<>();
        Material material = new Material();
        materialLibrary.put("", material);
        int materialIndex = -1;

        if (materialLib == null) {
            return materialLibrary;
        }

        String filePath = "models/"+modelName+"/" + materialLib;
        String materialId = null;
        float[] ambient = new float[3];
        float[] diffuse = new float[3];
        float[] specular = new float[3];
        float[] emissive = new float[3];
        float[] iof = new float[1];
        float[] shininess = new float[1];
        float transparency = 0;

        String textureFileName = null;

        BufferedReader bufferedReader;
        String line;
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(assetManager.open(
                            filePath), StandardCharsets.UTF_8));
            while ((line = bufferedReader.readLine()) != null) {
                String[] ls = line.split("\\s+");
                switch (ls[0]) {
                    case "newmtl":
                        if(materialIndex > -1) {
                            material.setMaterialName(materialId);
                            material.setAmbient(ambient);
                            material.setDiffuse(diffuse);
                            material.setSpecular(specular);
                            material.setShineness(shininess);
                            material.setEmission(emissive);
                            material.setRefraction(iof);
                            material.setTransparency(transparency);
                            material.setTextureFileName(textureFileName);
                            materialLibrary.put(materialId, material);
                        }
                        materialId = ls[1];
                        material = new Material();
                        materialIndex++;
                        break;
                    case "Ka":
                        ambient[0] = Float.parseFloat(ls[1]);
                        ambient[1] = Float.parseFloat(ls[2]);
                        ambient[2] = Float.parseFloat(ls[3]);
                        break;
                    case "Kd":
                        diffuse[0] = Float.parseFloat(ls[1]);
                        diffuse[1] = Float.parseFloat(ls[2]);
                        diffuse[2] = Float.parseFloat(ls[3]);
                        break;
                    case "Ks":
                        specular[0] = Float.parseFloat(ls[1]);
                        specular[1] = Float.parseFloat(ls[2]);
                        specular[2] = Float.parseFloat(ls[3]);
                        break;
                    case "Tr":
                        transparency = Float.parseFloat(ls[1]);
                        break;
                    case "Ns":
                        shininess[0] = Float.parseFloat(ls[1]);
                        break;
                    case "Ke":
                        emissive[0] = Float.parseFloat(ls[1]);
                        emissive[1] = Float.parseFloat(ls[2]);
                        emissive[2] = Float.parseFloat(ls[3]);
                        break;
                    case "Ni":
                        iof[0] = Float.parseFloat(ls[1]);
                        break;
                    case "map_Kd":
                        textureFileName = ls[1];
                        break;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("error trying to load file: "+filePath);
            e.printStackTrace();
        }

        material.setMaterialName(materialId);
        material.setAmbient(ambient);
        material.setDiffuse(diffuse);
        material.setSpecular(specular);
        material.setShineness(shininess);
        material.setEmission(emissive);
        material.setRefraction(iof);
        material.setTransparency(transparency);
        material.setTextureFileName(textureFileName);
        materialLibrary.put(materialId, material);

        return materialLibrary;
    }

    private class MeshData{

        private List<float[]> positionArray;
        private List<float[]> textureArray;
        private List<float[]> normalArray;
        private List<int[]> faceArray;
        private final String name;
        private String materialName;
        private MeshData(String name){
            this.name = name;
        }
        protected void setMaterialId(String materialName){
            this.materialName = materialName;
        }
        protected String getMaterialId(){
            return materialName;
        }
        protected String getName(){return name;}
        public void setPositionArray(List<float[]> positionArray) {
            this.positionArray = positionArray;
        }
        public void setTextureArray(List<float[]> textureArray) {
            this.textureArray = textureArray;
        }
        public void setNormalArray(List<float[]> normalArray) {
            this.normalArray = normalArray;
        }
        public void setFaceArray(List<int[]> faceArray) {
            this.faceArray = faceArray;
        }
        public List<float[]> getPositionList() {
            return positionArray;
        }
        public List<float[]> getTextureList() {
            return textureArray;
        }
        public List<float[]> getNormalList() {
            return normalArray;
        }
        public List<int[]> getFaceList() {
            return faceArray;
        }

    }
}
