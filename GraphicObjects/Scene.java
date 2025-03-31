package com.example.openglexemple.GraphicObjects;


import static com.example.openglexemple.Constants.BUTTON_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.BUTTON_INT_UNIFORMS;
import static com.example.openglexemple.Constants.DYNAMIC_ATTRIBUTES;
import static com.example.openglexemple.Constants.DYNAMIC_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.DYNAMIC_INT_UNIFORMS;
import static com.example.openglexemple.Constants.DYNAMIC_MAT4ARR_UNIFORMS;
import static com.example.openglexemple.Constants.OBJ_ATTRIBUTES;
import static com.example.openglexemple.Constants.POINT_ATTRIBUTES;
import static com.example.openglexemple.Constants.POINT_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.POINT_INT_UNIFORMS;
import static com.example.openglexemple.Constants.STATIC_FLOAT_UNIFORMS;
import static com.example.openglexemple.Constants.STATIC_INT_UNIFORMS;

import com.example.openglexemple.GameObjects.DynamicModel;
import com.example.openglexemple.Constants;
import com.example.openglexemple.Engine.Camera;
import com.example.openglexemple.Engine.Loader;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GameObjects.Button;
import com.example.openglexemple.GameObjects.GameObject;
import com.example.openglexemple.GameObjects.Light;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Messenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    private final String TAG = "SCENE";

    private final Map<Integer, ShaderProgram> shaderPrograms = new HashMap<>();
    private final List<GameObject> solidModelList = new ArrayList<>();
    private final List<GameObject> plyModelList = new ArrayList<>();
    private final List<GameObject> dynamicModelList = new ArrayList<>();
    private final List<GameObject> pointList = new ArrayList<>();
    private final List<GameObject> buttonsList = new ArrayList<>();
    private final Map<String, GameObject> gameObjectsMap = new HashMap<>();
    private Camera getCamera;
    private Loader loader;
    private float[] iorAmbient;

    private Camera camera;
    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }

    private Light light;
    private ShaderProgram plyProgram;
    private ShaderProgram objProgram;
    private ShaderProgram pointProgram;
    private ShaderProgram dynamicProgram;
    private ShaderProgram buttonProgram;

    /**
     *
     * @param loader
     * @param transformation
     */
    public Scene(Loader loader, Transformation transformation){
        plyProgram = new ShaderProgram("ply.vert", "ply.frag", Constants.PLY_ATTRIBUTES, loader);
        plyProgram.setAttributeHandles(Constants.PLY_ATTRIBUTES);
        plyProgram.setFloatUniformHandles(STATIC_FLOAT_UNIFORMS);
        plyProgram.setIntUniformHandles(STATIC_INT_UNIFORMS);

        objProgram = new ShaderProgram("obj.vert", "obj.frag", Constants.OBJ_ATTRIBUTES, loader);
        objProgram.setAttributeHandles(Constants.OBJ_ATTRIBUTES);
        objProgram.setFloatUniformHandles(STATIC_FLOAT_UNIFORMS);
        objProgram.setIntUniformHandles(STATIC_INT_UNIFORMS);

        dynamicProgram = new ShaderProgram("dynamic.vert", "dynamic.frag", DYNAMIC_ATTRIBUTES, loader);
        dynamicProgram.setAttributeHandles(DYNAMIC_ATTRIBUTES);
        dynamicProgram.setFloatUniformHandles(DYNAMIC_FLOAT_UNIFORMS);
        dynamicProgram.setIntUniformHandles(DYNAMIC_INT_UNIFORMS);
        dynamicProgram.setMat4ArrUniformHandles(DYNAMIC_MAT4ARR_UNIFORMS, Constants.MAX_JOINTS_TRANSFORMS );
        //dynamicProgram.setMat4ArrUniformHandles(DYNAMIC_MAT4ARR_UNIFORMS);

        pointProgram = new ShaderProgram("point_vertex_shader.vert", "point_fragment_shader.frag", POINT_ATTRIBUTES, loader);
        pointProgram.setAttributeHandles(POINT_ATTRIBUTES);
        pointProgram.setIntUniformHandles(POINT_INT_UNIFORMS);
        pointProgram.setFloatUniformHandles(POINT_FLOAT_UNIFORMS);

        buttonProgram = new ShaderProgram("button.vert", "button.frag", OBJ_ATTRIBUTES, loader);
        buttonProgram.setAttributeHandles(OBJ_ATTRIBUTES);
        buttonProgram.setIntUniformHandles(BUTTON_INT_UNIFORMS);
        buttonProgram.setFloatUniformHandles(BUTTON_FLOAT_UNIFORMS);

        //index is the program id
        if( plyProgram.getProgramHandle() > -1){
            shaderPrograms.put( plyProgram.getProgramHandle(), plyProgram);
        }
        if( objProgram.getProgramHandle() > -1){
            shaderPrograms.put( objProgram.getProgramHandle(), objProgram);
        }
        if( dynamicProgram.getProgramHandle() > -1){
            shaderPrograms.put( dynamicProgram.getProgramHandle(), dynamicProgram);
        }
        if(pointProgram.getProgramHandle() > -1){
            shaderPrograms.put(pointProgram.getProgramHandle(),pointProgram);
        }
        if(buttonProgram.getProgramHandle()>-1){
            shaderPrograms.put(buttonProgram.getProgramHandle(), buttonProgram);
        }

        camera = new Camera();

        light = new Light(transformation);
    }

    public void createGameObjects(Loader loader) {
        this.loader = loader;

        this.setIorAmbient(new float[]{1.0f});

        camera.setPosition(0,0,0);
/*
        SolidModel[] coloredCube = loader.loadSolidModels("colored_cube.ply", plyProgram);
        for(SolidModel plyModel: coloredCube) {
            plyModel.setPosition(0, -6, -9);
            plyModel.setAngularVelocity(36);
            plyModel.translate();
            this.getPlyModelList().add(plyModel);
        }
*/
/*
        SolidModel[] cube = loader.loadSolidModels("cube_dice.obj", objProgram);
        for(SolidModel solidModel : cube) {
            solidModel.setAngularVelocity(36f / 10);
            solidModel.setRotationAxis(0, 1, 0);
            solidModel.setPosition(-3, 6, -10);
            solidModel.setScale(new Vector3f(2, 2, 2));
            solidModelList.add(solidModel);
        }
*/
        //create buttons
        Button btnUp = loader.loadButton("btn_up", "button_direction.png", buttonProgram);
        btnUp.setPosition(-2, -5, -9);
        btnUp.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
        buttonsList.add(btnUp);

        Button btnDown = loader.loadButton("btn_down", "button_direction.png", buttonProgram);
        btnDown.setPosition(-2, -8, -9);
        btnDown.setRotation(180,0,0,1);
        btnDown.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
        buttonsList.add(btnDown);

        Button btnLeft = loader.loadButton("btn_left", "button_direction.png", buttonProgram);
        btnLeft.setPosition(-3.5f, -6.5f, -9);
        btnLeft.setRotation(-30,0,0,1);
        btnLeft.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
        buttonsList.add(btnLeft);

        Button btnRight = loader.loadButton("btn_right", "button_direction.png", buttonProgram);
        btnRight.setPosition(-0.5f, -6.5f, -9);
        btnRight.setRotation(30,0,0,1);
        btnRight.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
        buttonsList.add(btnRight);

        /*
        //create points
        Point point1 = new Point(new Vector3f(2,5,-7), Colors.WHITE, 20.0f, pointProgram);
        point1.setName("point_01");
        Point point2 = new Point(new Vector3f(0,0,-7), Colors.RED, 20.0f, pointProgram);
        point2.setName("point_02");
        pointList.add(point1);
        pointList.add(point2);

        //create polygons
        Polygon polygon = new Polygon(new Vector3f(0,7,-10), 3, Colors.GREEN, true, pointProgram);
        polygon.setName("Polygon-01");
        //polygon.setPointSize(10);
        //polygon.setAngularDisplacement(60,0,0);
        polygon.setAngularVelocity(30);
        polygon.setRotationAxis(0, 0, 1);
        polygon.createNormalLine();
        //------------ set bond to btnUp----------
        Messenger touch = new Messenger(Constants.INFO_TOUCHED);
        float[] btnUpAngleAxis = new float[]{polygon.getAngularVelocity(), 1,0,0};
        Messenger rotation = new Messenger(Constants.DO_ROTATE);
        Messenger[] bondsBtnUp2 = new Messenger[]{touch, null, rotation, new Messenger(btnUpAngleAxis)};
        polygon.setParentBonds(btnUp.getName(), bondsBtnUp2);
        //------------ set bond to btnLeft----------
        float[] btnLeftAngleAxis = new float[]{polygon.getAngularVelocity(),0,1,0};
        Messenger[] bondsBtnLeft2 = new Messenger[]{touch, null, rotation, new Messenger(btnLeftAngleAxis)};
        polygon.setParentBonds(btnLeft.getName(), bondsBtnLeft2);
        ///-------------------------------------------------
        Line polygonNormal = polygon.getNormalLine();
        pointList.add(polygon);
        pointList.add(polygonNormal);

        //create line
        Line line = new Line(new Vector3f[]{new Vector3f(), new Vector3f(1,0,0)}, Colors.RED, pointProgram);
        line.setPosition(0,-7,-10);
        line.setRotationAxis(0,1,0);
        line.setAngularVelocity(10);
        line.setUpdateByTime(true);
        line.setPointSize(10);
        pointList.add(line);


        /*
        SolidModel[] testPlatform = loader.loadSolidModels("platform.obj", objProgram);
        int scounter = 0;
        for(SolidModel model : testPlatform) {
            model.setPosition(0, -6+scounter, -15);
            model.translate();
            model.setRotationAxis(0, 0, 1);
            model.setAngularVelocity(360f / 20);
            solidModelList.add(model);
            scounter += 5;
        }
*/
        DynamicModel[] human = loader.loadDAE("human", dynamicProgram);
        for(DynamicModel go : human ){
            go.setPosition(new Vector3f(0,0, -7));
            go.setScale(new Vector3f(3f,3f,3f));
            go.setRotation(90,1,0,0);
            go.setAngularVelocity(0);
            go.setLinearVelocity(1,1,1);
            go.setUpdateByTime(true);
            //............setup button bonds..............
            Messenger touch = new Messenger(Constants.INFO_TOUCHED);
            float[] btnUpTranslation = new float[]{0,0,go.getLinearVelocity().z};
            Messenger translation = new Messenger(Constants.DO_TRANSLATE);
            Messenger[] bondsBtnUp = new Messenger[]{touch, null, translation, new Messenger(btnUpTranslation)};
            go.setParentBonds(btnUp.getName(), bondsBtnUp);
            //.............................................
            float[] btnDownTranslation = new float[]{0,0,-go.getLinearVelocity().z};
            Messenger[] bondsBtnDown = new Messenger[]{touch, null, translation, new Messenger(btnDownTranslation)};
            go.setParentBonds(btnDown.getName(), bondsBtnDown);
            //.............................................
             Messenger rotate = new Messenger(Constants.DO_ROTATE);
            float[] btnRightRotation = new float[]{10, 1, 0, 0};
            Messenger[] bondsBtnRight = new Messenger[]{touch, null, rotate, new Messenger(btnRightRotation)};
            go.setParentBonds(btnRight.getName(), bondsBtnRight);
            //.............................................
            float[] btnLeftRotation = new float[]{-10, 1,0,0};
            Messenger[] bondsBtnLeft = new Messenger[]{touch, null, rotate, new Messenger(btnLeftRotation)};
            go.setParentBonds(btnLeft.getName(), bondsBtnLeft);
            //.............................................
            getDynamicModelList().add(go);
        }

        System.out.println(TAG+" solid model list size: "+solidModelList.size());
        System.out.println(TAG + " dynamic model list size: "+dynamicModelList.size());
    }

   public List<GameObject> getSolidModelList(){
        return this.solidModelList;
    }
    public List<GameObject> getPlyModelList(){
        return this.plyModelList;
    }
    public List<GameObject> getPointList(){
        return pointList;
    }
    public List<GameObject> getDynamicModelList() {
        return dynamicModelList;
    }
    public List<GameObject> getButtonsList(){
        return buttonsList;
    }
    public Map<Integer, ShaderProgram> getShaderPrograms(){
        return shaderPrograms;
    }

    public Map<String, GameObject> getGameObjectsMap(){return gameObjectsMap;}
    public float[] getIorAmbient() {
        return this.iorAmbient;
    }
    public void setIorAmbient(float[] iorAmbient){
        this.iorAmbient = iorAmbient;
    }
    public ShaderProgram getPlyProgram(){
        return plyProgram;
    }
    public ShaderProgram getObjProgram(){
        return objProgram;
    }
    public ShaderProgram getPointProgram(){
        return pointProgram;
    }
    public ShaderProgram getDynamicProgram(){
        return dynamicProgram;
    }
    public ShaderProgram getButtonProgram(){
        return buttonProgram;
    }
    public Camera getCamera(){
        return camera;
    }
}

