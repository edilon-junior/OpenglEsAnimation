package com.example.openglexemple.GraphicObjects;

import android.opengl.GLES30;

public class Material {
    public static final String TAG = "MATERIAL";
    private float[] ambient;
    private float[] emission;
    private float[] diffuse;
    private float[] specular;
    float[] transparency;
    float[] shininess;
    float[] refraction;
    float optical_density;
    float dissolve;
    float illumination;
    String textureName;
    int[] textureId;
    int indexCount;
    int[] sample2Did;
    String id;
    String materialName;
    String instanceEffect;
    String diffuseMap;
    String[] textures;
    private int index;

    public Material(float[] ambient,
                    float[] diffuse,
                    float[] specular,
                    float shininess,
                    float transparency,
                    String textureName){
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = new float[]{shininess};
        this.transparency = new float[]{transparency};
        this.textureName = textureName;
        sample2Did = new int[1];
        indexCount = 0;
        materialName = "";
    }
    public Material(float[] emission,
                    float[] ambient,
                    float[] diffuse,
                    float[] specular,
                    float shininess,
                    float transparency){
        this.emission = emission;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = new float[]{shininess};
        this.transparency = new float[]{transparency};
        this.textureName = "";
        this.textureId = new int[1];
        indexCount = 0;
        sample2Did = new int[1];
        materialName = "";
    }
    public Material(float[] ambient,
                    float[] diffuse,
                    float[] specular,
                    float shininess,
                    float transparency){
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = new float[]{shininess};
        this.transparency = new float[]{transparency};
        this.textureName = "";
        this.textureId = new int[1];
        indexCount = 0;
        sample2Did = new int[1];
        materialName = "";
    }
    public Material() {
        this.ambient = new float[]{ 0,0,0};
        this.diffuse = new float[]{0,0,0};
        this.specular = new float[]{0,0,0};
        this.shininess = new float[]{0.1f};
        this.transparency = new float[]{0};
        this.textureName = "";
        this.textureId =  new int[1];
        indexCount = 0;
        sample2Did = new int[]{-1};
        materialName = "";
    }
    // id varies from 0 to 31
    public void setupSampler2d(int index){
        int id = this.getSample2Did()[index];
        GLES30.glActiveTexture(33984 + id);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id);
    }

    public void setAmbient(float[] ambient){
        this.ambient = ambient;
    }
    public void setEmission(float[] emission){
        this.emission = emission;
    }

    public float[] getEmission()
    {
        return this.emission;
    }
    public float[] getRefraction() {
        return refraction;
    }

    public void setRefraction(float[] refraction) {
        this.refraction = refraction;
    }
    public void setMaterialName(String materialName){
        this.materialName = materialName;
    }
    public String getMaterialName(){
        return materialName;
    }

    public String getTextureFileName() {
        return textureName;
    }

    public void setTextureFileName(String textureName) {
        this.textureName = textureName;
    }

    public int[] getTexture(){
        return this.textureId;
    }

    public void setTexture(int[] texture){
        this.textureId = texture;
    }
    public void setTransparency(float transparency){
        this.transparency[0] = transparency;
    }
    public float[] getAmbient(){
        return this.ambient;
    }
    public void setDiffuse(float[] diffuse){
        this.diffuse = diffuse;
    }
    public float[] getDiffuse(){
        return this.diffuse;
    }
    public float[] getSpecular(){
        return this.specular;
    }
    public float[] getShininess(){return this.shininess;}
    public float[] getTransparency(){return this.transparency;}

    /**
     *
     * @param indexCount the number of indices of triangles from model
     */
    public void setIndexCount(int indexCount){
        this.indexCount = indexCount;
    }
    public int getIndexCount(){
        return indexCount;
    }
    public int[] getSample2Did() {
        return sample2Did;
    }

    public void setSample2Did(int[] sample2Did) {
        this.sample2Did = sample2Did;
    }

    public String getInstanceEffect() {
        return instanceEffect;
    }

    public void setInstanceEffect(String instanceEffect) {
        this.instanceEffect = instanceEffect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiffuseMap() {
        return diffuseMap;
    }

    public void setDiffuseMap(String diffuseMap) {
        this.diffuseMap = diffuseMap;
    }

    public String[] getTextures() {
        return textures;
    }

    public void setTextures(String[] textures) {
        this.textures = textures;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public void setSpecular(float[] specular) {
        this.specular = specular;
    }

    public void setShineness(float[] shininess) {this.shininess = shininess;
    }

}
