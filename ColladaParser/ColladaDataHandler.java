package com.example.openglexemple.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Constants;
import com.example.openglexemple.Animation.Joint;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Utils;
import com.example.openglexemple.XmlParser.XmlNode;

import java.util.HashMap;
import java.util.Map;

public class ColladaDataHandler {

    public static final String TAG = "COLLADA_DATA_HANDLER";
    XmlNode model;
    XmlNode geometry;
    XmlNode skin;
    XmlNode armature;
    private float[][] jointsId;
    private float[][] vertexWeights;
    private Skeleton skeleton;
    private final Map<String, float[]> ibmMap = new HashMap<>();
    private final Map<String, Integer> jointIndices = new HashMap<>();
    private float[] bindShapeMatrix;

    public ColladaDataHandler(
            XmlNode model, XmlNode geometry){
        this.model = model;
        this.geometry = geometry;
    }
    public Mesh createMesh(float[] bindShapeMatrix ){
        if(skin != null) {
            rearrangeWeightsJoints();
        }else{
            jointsId = new float[0][0];
            vertexWeights = new float[0][0];
        }
        GeometryLoader geometryLoader = new GeometryLoader(geometry, jointsId, vertexWeights);
        geometryLoader.setBindShapeMatrix(bindShapeMatrix);
        //--------------------------------------------------------------
        Mesh mesh = geometryLoader.createMesh();
        mesh.setName(geometryLoader.getMeshName());
        mesh.setSemantics(geometryLoader.getSemantics());
        mesh.setMaterialId(geometryLoader.getMaterialId());
        mesh.setVertexStrider(geometryLoader.getVertexStride());
        mesh.setHomogeneous(true);
        if(skin != null){
            mesh.setMaxInfluences(Constants.MAX_INTERACTIONS);
        }
        return mesh;
    }

    public Material createMaterial(XmlNode materialNode, XmlNode effectNode){
        //---------------------- create material -------------------------------
        String materialId = materialNode.getAttribute("id");

        XmlNode profileCommonNode = effectNode.getChild("profile_COMMON");
        XmlNode lambertNode = profileCommonNode.getChild("technique").getChild("lambert");
        float[] emission = Utils.stringToFloatArray(lambertNode.getChild("emission").getChild("color").getData());
        String diffuseSampler = lambertNode.getChild("diffuse").getChild("texture").getAttribute("texture");
        float ior = Float.parseFloat(lambertNode.getChild("index_of_refraction").getChild("float").getData());
        String samplerSource = profileCommonNode.getChildWithAttribute("newparam", "sid", diffuseSampler).getChild("sampler2D").getChild("source").getData();
        String textureFileName = profileCommonNode.getChildWithAttribute("newparam", "sid", samplerSource).getChild("surface").getChild("init_from").getData();

        Material material = new Material();
        material.setId(materialId);

        material.setEmission(emission);
        material.setRefraction(new float[]{ior});
        material.setDiffuseMap(textureFileName);

        return material;
    }

    public void rearrangeWeightsJoints(){
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

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void createSkeleton() {
        String rootJointUrl = model.getChild("instance_controller").getChild("skeleton").getData();
        String rootJointId = rootJointUrl.substring(1);
        XmlNode rootJointNode = armature.getChildWithAttribute("node", "id", rootJointId);

        skeleton = new Skeleton(rootJointId);

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

        //create joints and add to skeleton
        createJoints(rootJointNode, "");
        //create inverse bind matrices
        float[] invBindTransform = new float[16];
        Matrix.setIdentityM(invBindTransform, 0);
        skeleton.getRootJoint().calcInverseBindTransform(invBindTransform);
        System.out.println(TAG+"+++++++++++++++++++++++++++++++++++++++");
        System.out.println(TAG+" skeleton root joint "+skeleton.getRootJoint().getName());
        System.out.println(TAG+"+++++++++++++++++++++++++++++++++++++++");
    }

    private Joint createJoints(XmlNode jointNode, String parentJointId){
        String id = jointNode.getAttribute("id");
        String name = jointNode.getAttribute("name");
        Integer indexInteger = jointIndices.get(name);
        int index = -1;
        if(indexInteger != null){
            index = indexInteger;
        }
        //transformation of joint without animation
        float[] transform = Utils.stringToFloatArray(jointNode.getChild("matrix").getData());
        float[] transpose = new float[16];
        Matrix.transposeM(transpose, 0, transform, 0);

        Joint joint = new Joint(id, name, index, transpose);
        joint.setParentId(parentJointId);
        //joint.setIBM(ibmMap.get(joint.getName()));
        for(XmlNode node : jointNode.getChildren("node")){
            joint.addChild(createJoints(node, id));
        }

       //System.out.println(TAG+" joint "+id+" parent is "+parentJointId);

        skeleton.addJoints(joint);

        return joint;
    }

    public void setSkin(XmlNode skin) {
        this.skin = skin;
    }

    public void setArmature(XmlNode armatureNode) {
        this.armature = armatureNode;
    }

    public void setBindShapeMatrix(float[] bindShapeMatrix) {
        this.bindShapeMatrix = bindShapeMatrix;
    }
}
