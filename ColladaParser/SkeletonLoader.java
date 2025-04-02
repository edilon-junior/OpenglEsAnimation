package com.example.openglexemple.Loader.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Animation.Joint;
import com.example.openglexemple.Animation.JointTransform;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SkeletonLoader {

    public static final String TAG = "SkeletonLoader";
    private static final float[] CORRECTION = new float[16];
    private final XmlNode meshNode;
    private final XmlNode armatureNode;
    private Skeleton skeleton;

    private Map<String, float[]> ibms;
    private Map<String, Integer> jointIndices;
    public SkeletonLoader(XmlNode armatureNode, XmlNode meshNode, Map<String, float[]> ibms, Map<String, Integer> jointIndices){
        this.meshNode = meshNode;
        this.armatureNode = armatureNode;
        this.ibms = ibms;
        this.jointIndices = jointIndices;
        Matrix.setIdentityM(CORRECTION, 0);
        Matrix.rotateM(CORRECTION, 0, (float) Math.toRadians(-90), 1.0f, 0.0f, 0.0f);
    }

    public void createSkeleton() {
        String rootJointUrl = meshNode.getChild("instance_controller").getChild("skeleton").getData();
        String rootJointId = rootJointUrl.substring(1);
        XmlNode rootJointNode = armatureNode.getChildWithAttribute("node", "id", rootJointId);

        skeleton = new Skeleton(rootJointId);

        //create joints and add to skeleton
        createJoints(rootJointNode, "");
        //create inverse bind matrices
        float[] invBindTransform = new float[16];
        Matrix.setIdentityM(invBindTransform, 0);
        skeleton.getRootJoint().calcInverseBindTransform(invBindTransform, skeleton);
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

        if(parentJointId.equalsIgnoreCase("")){
            //Matrix.multiplyMM(transpose, 0, CORRECTION,0, transpose, 0 );
        }

        Joint joint = new Joint(id, name, index, transpose);
        joint.setParentId(parentJointId);
        //joint.setIBM(ibmMap.get(joint.getName()));
        for(XmlNode node : jointNode.getChildren("node")){
            joint.addChild(createJoints(node, id));
        }

        skeleton.addJoints(joint);

        return joint;
    }

    public Skeleton getSkeleton(){
        return skeleton;
    }
}
