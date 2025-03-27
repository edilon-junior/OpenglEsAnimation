package com.example.openglexemple.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Constants;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Utils;
import com.example.openglexemple.XmlParser.XmlNode;

import java.util.ArrayList;
import java.util.List;

public class GeometryLoader {

    XmlNode geometry;
    XmlNode meshNode;
    XmlNode trianglesNode;
    private final String meshName;
    private String materialId;
    private String[] semantics;
    private List<String> semanticsList = new ArrayList<>();
    private final List<Vertex> verticesList = new ArrayList<>();
    float[] vertexArray;
    float[] positions;
    float[] normals;
    float[] textures;
    float[] colors;
    short[] indices;
    private final float[][] jointIds;
    private final float[][] weights;
    private final List<Integer> indicesList = new ArrayList<>();
    int vertexStride = 0;
    private float[] bindShapeMatrix;

    public GeometryLoader(XmlNode geometry, float[][] jointIds, float[][] weights){
        this.geometry = geometry;
        meshNode = geometry.getChild("mesh");
        meshName = geometry.getAttribute("name");
        trianglesNode = meshNode.getChild("triangles");
        this.jointIds = jointIds;
        this.weights = weights;
        vertexStride = 14;
    }

    public void setBindShapeMatrix(float[] bindShapeMatrix){
        this.bindShapeMatrix = bindShapeMatrix;
    }

    public Mesh createMesh(){
        readMeshData();
        assembleVertices();
        convertIndicesListToArray();
        createVertexArray();
        return new Mesh(positions, textures, normals, colors,vertexArray, indices);
    }

    private void readMeshData(){
        List<XmlNode> attributes = trianglesNode.getChildren("input");

        for(int i=0 ;i< attributes.size();i++){
            XmlNode attribute = attributes.get(i);
            String semantic = attribute.getAttribute("semantic");
            semanticsList.add(semantic);
            if(semantic.equalsIgnoreCase("VERTEX")){
                XmlNode verticesNode = meshNode.getChild("vertices");
                String posSource = verticesNode.getChild("input").getAttribute("source").substring(1);
                XmlNode posNode = meshNode.getChildWithAttribute("source", "id", posSource);
                String rawPositions = posNode.getChild("float_array").getData();
                int posStride = Integer.parseInt(posNode.getChild("technique_common").getChild("accessor").getAttribute("stride"));
                positions = Utils.stringToFloatArray(rawPositions);
                //create vertices
                for(int j=0; j < positions.length / posStride; j++){
                    verticesList.add(new Vertex(verticesList.size(), j));
                }
            }
            if(semantic.equalsIgnoreCase("NORMAL")){
                String norSource = attribute.getAttribute("source").substring(1);
                XmlNode norNode = meshNode.getChildWithAttribute("source", "id", norSource);
                String rawNormals = norNode.getChild("float_array").getData();
                normals = Utils.stringToFloatArray(rawNormals);
            }
            if(semantic.equalsIgnoreCase("TEXCOORD")){
                String texSource = attribute.getAttribute("source").substring(1);
                XmlNode texNode = meshNode.getChildWithAttribute("source", "id", texSource);
                String rawTextures = texNode.getChild("float_array").getData();
                textures = Utils.stringToFloatArray(rawTextures);
            }
            if(semantic.equalsIgnoreCase("COLOR")){
                String colSource = attribute.getAttribute("source").substring(1);
                XmlNode colNode = meshNode.getChildWithAttribute("source", "id", colSource);
                String rawColors = colNode.getChild("float_array").getData();
                colors = Utils.stringToFloatArray(rawColors);
            }
        }
    }

    private void assembleVertices(){
        String[] attributeIndices = trianglesNode.getChild("p").getData().split("\\s+");
        int attribStride = semanticsList.size();
        for(int i=0; i < attributeIndices.length / attribStride; i++){
            int positionIndex = Integer.parseInt(attributeIndices[i * attribStride]);
            int normalIndex = Integer.parseInt(attributeIndices[i * attribStride + 1]);
            int texCoordIndex = Integer.parseInt(attributeIndices[i * attribStride + 2]);
            int colorIndex = -1;
            if(attribStride > 3) {
                colorIndex = Integer.parseInt(attributeIndices[i * attribStride + 3]);
            }
            Vertex t = processVertex(positionIndex, normalIndex, texCoordIndex, colorIndex);
            //System.out.println("geometryLoader vertex: " +t);
        }
    }
    private Vertex processVertex(int pIndex, int nIndex, int tIndex, int cIndex){
        Vertex currentVertex = verticesList.get(pIndex);
        //on the first iteration, all vertex are able to receive data
        if(!currentVertex.isSet()){
            currentVertex.setNormal(nIndex);
            currentVertex.setUv(tIndex);
            currentVertex.setColor(cIndex);
            indicesList.add(pIndex);
            return currentVertex;
        }else{
            //ops! already exists a vertex in this position. lets set in another position
            return dealWithAlreadyProcessedVertex(currentVertex, nIndex, tIndex, cIndex);
        }
    }
    private Vertex dealWithAlreadyProcessedVertex(Vertex previousVertex, int n , int t, int c) {
        //ok this vertex are equals to the we want to set in this position
        if (previousVertex.equals(n, t, c)) {
            indicesList.add(previousVertex.getIndex());
            return previousVertex;
            // in this case, this vertex has same position index to another set vertex. it's a problem!
            // we need to set in a new position on vertex list
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                return dealWithAlreadyProcessedVertex(anotherVertex, n, t, c);
            } else {
                Vertex duplicateVertex = new Vertex(verticesList.size(), previousVertex.getPosition());
                duplicateVertex.setNormal(n);
                duplicateVertex.setUv(t);
                duplicateVertex.setColor(c);
                previousVertex.setDuplicateVertex(duplicateVertex);
                verticesList.add(duplicateVertex);
                indicesList.add(duplicateVertex.getIndex());
                return duplicateVertex;
            }
        }
    }

    private void convertIndicesListToArray() {
        this.indices = new short[indicesList.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indicesList.get(i).shortValue();
        }
    }
    private void createVertexArray(){
        int mod = 0;
        if(jointIds.length > 0) {
            vertexStride += Constants.MAX_INTERACTIONS *2;
        }
        if(colors == null){
            semanticsList.add("COLOR");
        }
        semantics = semanticsList.toArray(new String[0]);

        System.out.println("geometryLoader strider : "+vertexStride);
        vertexArray = new float[verticesList.size() * vertexStride];
        for (int i = 0; i < verticesList.size(); i++) {
            Vertex v  = verticesList.get(i);
            //get attribute indices
            //int vi = v.getIndex();
            int pi = v.getPosition();
            int ni = v.getNormal();
            int ti = v.getUv();
            int ci = v.getColor();

            float[] newPos = new float[]{positions[pi * 3], positions[pi * 3+1], positions[pi * 3+2], 1.0f};
            if(bindShapeMatrix != null){
                Matrix.multiplyMV(newPos, 0, bindShapeMatrix, 0, newPos, 0);
            }

            vertexArray[i * vertexStride] = newPos[0];
            vertexArray[i * vertexStride + 1] = newPos[1];
            vertexArray[i * vertexStride + 2] = newPos[2];
            vertexArray[i * vertexStride + 3] = 1.0f;
            vertexArray[i * vertexStride + 4] = normals[ni * 3];
            vertexArray[i * vertexStride + 5] = normals[ni * 3 +1];
            vertexArray[i * vertexStride + 6] = normals[ni * 3 +2];
            vertexArray[i * vertexStride + 7] = 1.0f;
            vertexArray[i * vertexStride + 8] = textures[ti * 2];
            vertexArray[i * vertexStride + 9] = textures[ti * 2 +1];
            if(colors != null) {
                vertexArray[i * vertexStride + 10] = colors[ci * 3];
                vertexArray[i * vertexStride + 11] = colors[ci * 3 + 1];
                vertexArray[i * vertexStride + 12] = colors[ci * 3 + 2];
                vertexArray[i * vertexStride + 13] = colors[ci * 3 + 3];
            }else{
                vertexArray[i * vertexStride + 10] = -1.0f;
                vertexArray[i * vertexStride + 11] = -1.0f;
                vertexArray[i * vertexStride + 12] = -1.0f;
                vertexArray[i * vertexStride + 13] = -1.0f;
            }
            if(jointIds.length > 0) {
                float[] vJoints = jointIds[pi];
                float[] vWeights = weights[pi];
                vertexArray[i * vertexStride + 14] = vJoints[0];
                vertexArray[i * vertexStride + 15] = vJoints[1];
                vertexArray[i * vertexStride + 16] = vJoints[2];
                vertexArray[i * vertexStride + 17] = vJoints[3];
                vertexArray[i * vertexStride + 18] = vWeights[0];
                vertexArray[i * vertexStride + 19] = vWeights[1];
                vertexArray[i * vertexStride + 20] = vWeights[2];
                vertexArray[i * vertexStride + 21] = vWeights[3];
            }
        }
    }

    public String getMeshName(){return meshName;}
    public String getMaterialId(){return materialId;}
    public String[] getSemantics() {return semantics;
    }
    public int getVertexStride(){return vertexStride;}
}
