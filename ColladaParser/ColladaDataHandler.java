package com.example.openglexemple.Loader.ColladaParser;

import android.opengl.Matrix;

import com.example.openglexemple.Animation.Animation;
import com.example.openglexemple.Utils.Constants;
import com.example.openglexemple.Animation.Joint;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Utils.Utils;
import com.example.openglexemple.Loader.XmlParser.XmlNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColladaDataHandler {

    public static final String TAG = "COLLADA_DATA_HANDLER";
    private final String sceneNodeId;
    private final XmlNode sceneNode;
    private XmlNode armatureNode;
    private final XmlNode meshNode;
    private final XmlNode libraryEffects;
    private final XmlNode libraryImages;
    private final XmlNode libraryMaterial;
    private final XmlNode libraryGeometries;
    private final XmlNode libraryControllers;
    private final XmlNode libraryAnimations;
    private final XmlNode geometry;
    private final XmlNode controller;
    private XmlNode materialNode;
    private XmlNode effect;
    XmlNode skin;
    private final XmlNode animationNode;

    public ColladaDataHandler(XmlNode sceneNode, XmlNode colladaData){
        libraryEffects = colladaData.getChild("library_effects");
        libraryImages = colladaData.getChild("library_images");
        libraryMaterial = colladaData.getChild("library_materials");
        libraryGeometries = colladaData.getChild("library_geometries");
        libraryControllers = colladaData.getChild("library_controllers");
        libraryAnimations = colladaData.getChild("library_animations");
        sceneNodeId = sceneNode.getAttribute("id");
        this.sceneNode = sceneNode;
        System.out.println(TAG+" node id: "+sceneNodeId);
        if(sceneNodeId.equalsIgnoreCase("Armature")){
            armatureNode = sceneNode;
            System.out.println(TAG+" armature: "+ sceneNode.getAttribute("id"));
            meshNode = findMeshNode(sceneNode);
            System.out.println(TAG+" meshNode: "+ meshNode.getAttribute("id"));
        }else {
            meshNode = sceneNode;
        }
        controller = findController();

        if(controller == null) {
            geometry = findGeometry();
        }else{
            geometry = findGeometryByController();
        }
        animationNode = findAnimation();
    }

    public XmlNode findMeshNode(XmlNode node){
        boolean hasController = node.getChild("instance_controller")!=null;
        boolean hasGeometry = node.getChild("instance_geometry")!= null;

        if(hasController || hasGeometry){
            return node;
        }
        XmlNode child = node.getChildWithAttribute("node", "type","NODE");

        return findMeshNode(child);
    }

    public XmlNode findController(){
        XmlNode instanceController = meshNode.getChild("instance_controller");
        if(instanceController == null){
            return null;
        }
        materialNode = findMaterial(instanceController);
        effect = findEffect(materialNode);
        String instanceControllerUrl = instanceController.getAttribute("url").substring(1);
        XmlNode controller = libraryControllers.getChildWithAttribute("controller","id",instanceControllerUrl);
        return controller;
    }
    public XmlNode findGeometry(){
        XmlNode instanceGeometryNode = sceneNode.getChild("instance_geometry");
        if(instanceGeometryNode == null){
            return null;
        }
        materialNode = findMaterial(instanceGeometryNode);
        effect = findEffect(materialNode);
        String instanceGeometryUrl = instanceGeometryNode.getAttribute("url").substring(1);
        XmlNode geometry = libraryGeometries.getChildWithAttribute("geometry", "id", instanceGeometryUrl);
        return geometry;
    }
    public XmlNode findGeometryByController(){
        String meshSource = controller.getChild("skin").getAttribute("source").substring(1);
        return libraryGeometries.getChildWithAttribute("geometry", "id", meshSource);
    }

    public XmlNode findAnimation(){
        if(controller == null){return null;}
        return libraryAnimations.getChildWithAttribute("animation","name",controller.getAttribute("name"));
    }

    public XmlNode findMaterial(XmlNode node){
        String materialTarget = node.getChild("bind_material")
                .getChild("technique_common")
                .getChild("instance_material")
                .getAttribute("target").substring(1);
        return libraryMaterial.getChildWithAttribute("material", "id", materialTarget);
    }

    public XmlNode findEffect(XmlNode node){
        String effectUrl = node.getChild("instance_effect").getAttribute("url").substring(1);
        return libraryEffects.getChildWithAttribute("effect", "id", effectUrl);
    }

    //get image file name
    public String findImage(String fileName) {
        return libraryImages.getChildWithAttribute("image", "id", fileName).getChild("init_from").getData();
    }
    public XmlNode getController() {
        return controller;
    }
    public XmlNode getArmatureNode(){
        return armatureNode;
    }
    public XmlNode getMeshNode(){
        return meshNode;
    }

    public XmlNode getAnimation() {
        return animationNode;
    }

    public XmlNode getGeometry() {
        return geometry;
    }
    public XmlNode getMaterialNode(){
        return materialNode;
    }
    public XmlNode getEffect(){
        return effect;
    }
}
