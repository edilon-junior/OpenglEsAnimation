package com.example.openglexemple.Loader.ColladaParser;

import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Utils.Utils;

import java.util.Map;

public class MaterialLoader {

    Material material;
    XmlNode materialNode;
    XmlNode effectNode;
    public MaterialLoader(XmlNode materialNode, XmlNode effectNode){
        this.materialNode = materialNode;
        this.effectNode = effectNode;
    }
    public Material createMaterial(){
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
}
