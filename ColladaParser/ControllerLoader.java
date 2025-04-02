package com.example.openglexemple.Loader.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Utils.Constants;
import com.example.openglexemple.Utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ControllerLoader {

    private XmlNode skin;
    private float[][] jointsId;
    private float[][] vertexWeights;
    private final float[] bindShapeMatrix = new float[16];
    private final Map<String, float[]> ibmMap = new HashMap<>();
    private final Map<String, Integer> jointIndices = new HashMap<>();
    public ControllerLoader(XmlNode controller){
        findSkin(controller);
        findBindShapeMatrix();
        createJointIndicesAndIBM();
        createWeightsJoints();
    }
    public void findSkin(XmlNode controller){
        skin = controller.getChild("skin");
    }
    private void createJointIndicesAndIBM(){
        XmlNode jointsNode = skin.getChild("joints");
        String jointNamesSource = jointsNode.getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source").substring(1);
        String ibmSource = jointsNode.getChildWithAttribute("input", "semantic", "INV_BIND_MATRIX").getAttribute("source").substring(1);

        //get inverse bind matrices of each joint, ibm
        String[] jointNames = skin.getChildWithAttribute("source", "id", jointNamesSource).getChild("Name_array").getData().split("\\s+");
        XmlNode ibmNode = skin.getChildWithAttribute("source", "id", ibmSource);
        float[] ibmArr = Utils.stringToFloatArray(ibmNode.getChild("float_array").getData());
        int ibmStride = Integer.parseInt(ibmNode.getChild("technique_common").getChild("accessor").getAttribute("stride"));

        //get inverse bind matrices and joints indices from controller

        for (int i = 0; i < jointNames.length; i++) {
            String jointName = jointNames[i];

            float[] ibm = new float[ibmStride];
            for (int j = 0; j < ibmStride; j++) {
                ibm[j] = ibmArr[i * ibmStride + j];
            }

            //transpose ibm matrix
            float[] transposeibm = new float[16];
            Matrix.transposeM(transposeibm, 0, ibm, 0);
            ibmMap.put(jointName, transposeibm);
            jointIndices.put(jointNames[i], i);
        }
    }

    public void findBindShapeMatrix(){
        float[] noTransposedMatrix = Utils.stringToFloatArray(skin.getChild("bind_shape_matrix").getData());
        Matrix.transposeM(bindShapeMatrix, 0, noTransposedMatrix, 0);
    }
    public void createWeightsJoints(){
        XmlNode vertexWeightsNode = skin.getChild("vertex_weights");
        XmlNode vcountNode = vertexWeightsNode.getChild("vcount");
        XmlNode vNode = vertexWeightsNode.getChild("v");
        String weightSource = vertexWeightsNode.getChildWithAttribute(
                "input","semantic","WEIGHT").getAttribute("source").substring(1);
        XmlNode sourceWeights = skin.getChildWithAttribute("source", "id", weightSource);
        String[] vcount = vcountNode.getData().split("\\s+");
        String[] v = vNode.getData().split("\\s+");
        String[] weights = sourceWeights.getChild("float_array").getData().split("\\s+");

        jointsId = new float[vcount.length][Constants.MAX_INTERACTIONS];
        vertexWeights = new float[vcount.length][Constants.MAX_INTERACTIONS];

        int vcountSum = 0;

        for(int i=0; i < vcount.length; i++){
            int interactions = Integer.parseInt(vcount[i]);

            for(int k=0; k < Constants.MAX_INTERACTIONS;k++){
                int vPos = vcountSum * 2;

                if(k < interactions) {
                    jointsId[i][k] = Integer.parseInt(v[vPos + k * 2]);
                    vertexWeights[i][k] = Float.parseFloat(
                            weights[Integer.parseInt(v[vPos + k * 2 + 1])]);
                }else{
                    jointsId[i][k] = -1;
                    vertexWeights[i][k] = 0;
                }
            }
            vcountSum += interactions;
        }
    }

    public float[] getBindShapeMatrix(){
        return bindShapeMatrix;
    }
    public Map<String, float[]> getIbmMap(){
        return ibmMap;
    }
    public Map<String, Integer> getJointIndices(){
        return jointIndices;
    }
    public float[][] getJointsId(){
        return jointsId;
    }
    public float[][] getVertexWeights(){
        return vertexWeights;
    }

}
