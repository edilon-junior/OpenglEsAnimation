package com.example.openglexemple.GameObjects;

import android.opengl.Matrix;

import com.example.openglexemple.Animation.Quaternion;
import com.example.openglexemple.Colors;
import com.example.openglexemple.Constants;
import com.example.openglexemple.Engine.Camera;
import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Messenger;
import com.example.openglexemple.GraphicObjects.Scene;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GameObject {

    private static final String TAG = "GAME_OBJECT";
    private String name;
    private boolean updateModelMatrix;
    private int meshIndex;
    private int shaderProgramId;
    private float angularDisplacement = 0;
    private float lastAngularDisplacement;
    private Vector3f linearVelocity = new Vector3f();;
    private float angularVelocity;
    private float initialRotTime;
    private float currentRotTime = 0;
    private final float[] translateMatrix = new float[16];
    private final float[] rotateMatrix = new float[16];
    private final float[] scaleMatrix = new float[16];
    private final float[]  modelMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private float[] color = new float[4];
    private final float[] pointSize = new float[1];
    private final Vector3f scale = new Vector3f();
    private final Vector3f lastScale = new Vector3f();
    private final Vector3f initialPosition = new Vector3f();;
    private final Vector3f lastPosition = new Vector3f();
    private final Vector3f position = new Vector3f();
    private final Vector3f rotationAxis = new Vector3f();
    private final Vector3f translationalVelocity = new Vector3f();;
    private final Quaternion rotation;
    private float translateParam;
    private float rotParam;
    private final List<Mesh> meshes;
    private boolean updateByTime;
    private int selected = 0;
    private int touchCounter = 0;
    private int touchLimiter = 0;
    private boolean touched = false;
    private float revolutions = 0;
    private boolean tangible = false;
    private boolean hasTransparency = false;
    private final Map<String, Messenger[]> parentBond = new HashMap<>();

    public GameObject(){
        setUpdateModelMatrix(true);
        meshIndex = -1;
        shaderProgramId = -1;
        setRotationAxis(1,0,0);
        rotation = new Quaternion(rotationAxis, 0);
        setAngularDisplacement(0);
        setLastAngularDisplacement(0);
        setColor(Colors.GRAY);
        setScale(1.0f, 1.0f, 1.0f);
        setAngularVelocity(0);
        setInitialRotTime(0);
        setCurrentRotTime(0);
        setRotParam(0);
        setTranslateParam(0);
        initMatrices();
        meshes = new ArrayList<>();
        setUpdateByTime(false);
    }

    private void setCurrentRotTime(float currentRotTime) {
        this.currentRotTime = currentRotTime;
    }

    public void initMatrices(){
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
    }

    public Map<String,Messenger[]> getParentBonds(){
        return parentBond;
    }

    /**
     *
     * @param parentId -> the id of bonded object to collect data
     * @param bonds -> bond[0] is the received data to change property in bond[2];
     *                 <p> bond[1] is the modifier of received data;</p>
     *                 <p>bond[2] is the property of this object to change;</p>
     *              <p>bond[3] is the modifier of property to change.</p>
     */
    public void setParentBonds(String parentId, Messenger[] bonds){
        parentBond.put(parentId, bonds);
    }

    public void updateBonds(Map<String, GameObject> gameObjectMap){
        if(getParentBonds().size() == 0){return;}
        for(Map.Entry<String, Messenger[]> entry : getParentBonds().entrySet()){
            String parentName = entry.getKey();
            GameObject parent = gameObjectMap.get(parentName);
            if(parent == null){
                System.out.println(TAG+" parent : "+parentName+ " of "+getName()+" don't exist");
                return;
            }
            Messenger[] bonds = entry.getValue();
            byte propertyReceived = bonds[0].getByteVal();
            Messenger mod1 = bonds[1];
            byte propertyToChange = bonds[2].getByteVal();
            Messenger mod2 = bonds[3];

            Messenger valueToSend;

            switch (propertyReceived){
                case Constants.INFO_POSITION:
                    break;
                case Constants.INFO_ROTATION:
                    break;
                case Constants.INFO_TOUCH_COUNTER:
                    float val = parent.getTouchCounter();
                    valueToSend = new Messenger(Constants.TYPE_FLOAT);
                    valueToSend.setFloatVal(val);
                    setBond(propertyToChange, valueToSend, mod2);
                    break;
                case Constants.INFO_MODEL_MATRIX:
                    float[] mMatrix = parent.getModelMatrix();
                    float[] mvpMatrix = parent.getMvpMatrix();
                    valueToSend = new Messenger(mMatrix);
                    Messenger modToSend = new Messenger(mvpMatrix);
                    setBond(propertyToChange, valueToSend, modToSend);
                    break;
                case Constants.INFO_TOUCHED:
                    boolean touch = parent.isTouched();
                    valueToSend = new Messenger(touch);
                    setBond(propertyToChange, valueToSend, mod2);
            }
        }
    }
    private void setBond(byte valueToChange, Messenger valueReceived, Messenger modifier){
        float[] mod;
        switch (valueToChange){
            case Constants.INFO_ROTPARAM:
                if(valueReceived.getType()!=Constants.TYPE_FLOAT){
                    break;
                }
                setRotParam(valueReceived.getFloatVal());
                break;
            case Constants.INFO_MODEL_MATRIX:
                float[] matrix = valueReceived.getFloatArray();
                if(matrix == null){break;}
                setModelMatrix(matrix);
                setMVPMatrix(modifier.getFloatArray());
                setUpdateModelMatrix(false);
                break;
            case Constants.INFO_ROTATION_AXIS:
                setRotationAxis(modifier.getFloatArray());
                break;
            case Constants.DO_ROTATE:
                mod = modifier.getFloatArray();
                float angle = mod[0];
                float axisX = mod[1];
                float axisY = mod[2];
                float axisZ = mod[3];
                if(valueReceived.getBooleanVal()) {
                    rotate(angle, axisX, axisY, axisZ);
                }
                break;
            case Constants.DO_TRANSLATE:
                mod = modifier.getFloatArray();
                if(valueReceived.getBooleanVal()){
                    translate(mod[0], mod[1], mod[2]);
                }
                break;
        }
    }

    public void setUpdateModelMatrix(boolean updateModelMatrix){
        this.updateModelMatrix = updateModelMatrix;
    }
    public boolean isUpdateModelMatrix(){
        return updateModelMatrix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(float[] color){
        this.color = color;
    }
    public float[] getColor() {
        return color;
    }
    public void setPointSize(float size) {
        this.pointSize[0] = size;
    }

    public float[] getPointSize(){
        return this.pointSize;
    }

    public void setShaderProgramId(int shaderProgramId){
        this.shaderProgramId = shaderProgramId;
    }
    public int getShaderProgramId(){
        return shaderProgramId;
    }
    public void setMesh(List<Mesh> mesh){
        this.meshes.addAll(mesh);
    }
    public List<Mesh> getMeshList(){
        return this.meshes;
    }
    public Mesh getMesh(int index){
        return this.meshes.get(index);
    }
    public void setMeshIndex(int index){
        this.meshIndex = index;
    }
    public int getMeshIndex(){
        return this.meshIndex;
    }
    public void setInitialRotTime(float initialRotTime){
        this.initialRotTime = initialRotTime;
    }
    public float getInitialRotTime() {
        return initialRotTime;
    }

    public void setLinearVelocity(float vx, float vy, float vz){
        this.linearVelocity.set(vx,vy,vz);
    }

    public Vector3f getLinearVelocity(){
        return this.linearVelocity;
    }

    /**
     *
     * @return linear velocity module
     */
    public float getLVM(){
        return this.getLinearVelocity().length();
    }

    public float getAngularVelocity(){
        return angularVelocity;
    }

    public void setAngularVelocity(float angular_velocity){
        this.angularVelocity = angular_velocity;
        this.setUpdateModelMatrix(true);
    }

    public void setModelMatrix(float[] modelMatrix){
        System.arraycopy(modelMatrix, 0, this.modelMatrix, 0, modelMatrix.length);
    }

    public float[] getModelMatrix(){
        return this.modelMatrix;
    }

    public void setMVPMatrix(float[] mvpMatrix){
        System.arraycopy(mvpMatrix, 0, this.mvpMatrix, 0, mvpMatrix.length);
    }

    public float[] getMvpMatrix(){
        return mvpMatrix;
    }

    public void setRotation(float a, float x, float y, float z){
        Matrix.setIdentityM(rotateMatrix,0);
        setAngularDisplacement(a);
        setRotationAxis(x,y,z);
        rotate(a,x,y,z);
        setUpdateModelMatrix(true);
    }
    public void setRotationAxis(float x, float y, float z){
        this.rotationAxis.set(x,y,z);
    }
    public void setRotationAxis(float[] floatArr){
        setRotationAxis(floatArr[0], floatArr[1], floatArr[2]);
    }
    public float getLastAngularDisplacement() {
        return lastAngularDisplacement;
    }

    public void setLastAngularDisplacement(float lastAngularDisplacement) {
        this.lastAngularDisplacement = lastAngularDisplacement;
    }
    public void setAngularDisplacement(float theta){
        angularDisplacement = theta;
        setUpdateModelMatrix(true);
    }

    public void addAngularDisplacement(float theta){
        angularDisplacement = angularDisplacement + theta;
        setUpdateModelMatrix(true);
    }
    public float getAngularDisplacement() {
        return angularDisplacement;
    }
    public Vector3f getRotationAxis(){
        return this.rotationAxis.clone();
    }

    public void setPosition(Vector3f position){
        Matrix.setIdentityM(translateMatrix,0);
        translate(position.x, position.y, position.z);
        this.position.set(position);
        setUpdateModelMatrix(true);
    }

    public void setPosition(float x, float y, float z){
        Matrix.setIdentityM(translateMatrix,0);
        translate(x,y,z);
        lastPosition.set(position);
        position.set(x,y,z);
        setUpdateModelMatrix(true);
    }

    public Vector3f getPosition(){
        return position.clone();
    }

    public Vector3f getLastPosition(){return lastPosition;}
    public void setLastPosition(Vector3f lastPosition){
        this.lastPosition.set(lastPosition);
    }
    public void setScale(Vector3f scale){
        this.scale.set(scale);
        setUpdateModelMatrix(true);
    }
    public void setScale(float x, float y, float z){
        scale.set(x,y,z);
        Matrix.setIdentityM(scaleMatrix,0);
        scale(x,y,z);
        setUpdateModelMatrix(true);
    }
    public Vector3f getScale(){
        return this.scale.clone();
    }

    public int isSelected() {
        return selected;
    }

    public void setSelected(int isSelected) {
        this.selected = isSelected;
    }

    public void setRevolutions(float revolutions){
        this.revolutions = revolutions;
    }
    public float getRevolutions(){return revolutions;}
    public void setTouchCounter(int touchCounter){
        this.touchCounter = touchCounter;
    }
    public int getTouchCounter(){
        return touchCounter;
    }
    public void setTangible(boolean val){
        this.tangible = val;
    }
    public boolean isTangible(){
        return tangible;
    }
    public boolean isUpdateByTime(){return updateByTime;}
    public void setUpdateByTime(boolean updateByTime){
        this.updateByTime = updateByTime;
    }
    public void setTranslateParam(float translateParam){
        this.translateParam = translateParam;
    }
    public float getTranslateParam(){
        return translateParam;
    }
    public void setRotParam(float rotParam){
        this.rotParam = rotParam;
    }
    public float getRotParam(){
        return rotParam;
    }
    public boolean isHasTransparency() {
        return hasTransparency;
    }
    public void setHasTransparency(boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }
    public boolean isTouched() {
        return touched;
    }
    public void setTouched(boolean touched) {
        this.touched = touched;
    }
    public int getTouchLimiter() {
        return touchLimiter;
    }
    public void setTouchLimiter(int touchLimiter) {
        this.touchLimiter = touchLimiter;
    }

    public void translate(float x, float y, float z){
        Matrix.translateM(translateMatrix, 0,x, y, z);
        setUpdateModelMatrix(true);
    }
    public void rotate(float angle, float x, float y, float z){
        Matrix.rotateM(rotateMatrix,0,angle,x,y,z);
        setUpdateModelMatrix(true);
    }

    public void scale(float x, float y, float z){
        Matrix.scaleM(scaleMatrix, 0, x,y,z);
        setUpdateModelMatrix(true);
    }

    public void updateTranslation(Timer timer){
        float dt = getTranslateParam();
        Vector3f ds = getLinearVelocity().getMul(dt);
        float dsLength = ds.length();
        if(dsLength != 0) {
            translate(ds.x, ds.y, ds.z);
            setLastPosition(getPosition().getAdd(ds));
        }
    }
    public void updateRotation(Timer timer) {
        float dt = 0;

        if(getInitialRotTime()==0){
            setInitialRotTime(timer.getSeconds());
        }

        if(isUpdateByTime()) {
            dt = timer.getDeltaTime();
        }

        if(dt == 0){return;}

        float angularDisplacement = getAngularVelocity() * dt;
        float dA = angularDisplacement - lastAngularDisplacement;
        if(dA != 0) {
            setAngularDisplacement(angularDisplacement);
            rotate(getAngularDisplacement(), getRotationAxis().x, getRotationAxis().y, getRotationAxis().z);
            setLastAngularDisplacement(getAngularDisplacement());
        }
        //when complete one cycle
        if (angularDisplacement > 360 ) {
            setInitialRotTime(timer.getSeconds());
        }
    }
    public void updateScale(){
        if(lastScale.equals(getScale())){return;}
        Matrix.setIdentityM(scaleMatrix, 0);
        scale(getScale().x, getScale().y, getScale().z);
        lastScale.set(getScale());
    }
    public void updateModelMatrix(Transformation transformation){
        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, translateMatrix, 0, rotateMatrix, 0);
        Matrix.multiplyMM(temp, 0, temp,0 , scaleMatrix, 0);
        setModelMatrix(temp);
        setMVPMatrix(transformation.getMVPMatrix(getModelMatrix()));
        setUpdateModelMatrix(false);
    }

    public void prepare(Timer timer){
        setTouched(false);
    }
    public void update(){}
    public void update(Timer timer){}
    public void update(Timer timer, Input input){}
    public void update(Timer timer, Transformation transformation){};

    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input){};
    public void update(Map<String, GameObject> gameObjectMap){}

    public abstract void update(Timer timer,
                                Transformation transformation,
                                MousePicker mousePicker,
                                Input input, Map<String,
            GameObject> gameObjectMap);

    public void render(Scene scene, Transformation transformation){}
    public void render(Scene scene, Transformation transformation, Camera camera){}
    public void render(ShaderProgram shaderProgram, Transformation transformation){}
    public void cleanup(){

    }
}
