package com.example.openglexemple.GameObjects;

import static com.example.openglexemple.Utils.Constants.BYTES_PER_SHORT;
import static com.example.openglexemple.Utils.Constants.DYNAMIC_FLOAT_UNIFORMS;
import static com.example.openglexemple.Utils.Constants.DYNAMIC_INT_UNIFORMS;
import static com.example.openglexemple.Utils.Constants.UNIFORM_JT_MATRIX;

import android.opengl.GLES30;

import com.example.openglexemple.Animation.Animation;
import com.example.openglexemple.Animation.Joint;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Engine.Camera;
import com.example.openglexemple.GraphicObjects.Light;
import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Utils.Constants;
import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.GraphicObjects.Scene;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Timer;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.Utils.Utils;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

public class DynamicModel extends GameObject {
    private static final String TAG = "DYNAMIC_MODEL";
    private Animation animation;
    private Skeleton skeleton;

    //current animation joint transforms
    private FloatBuffer jointTransformsBuffer;
    private final float[][] jointTransforms;

    public DynamicModel(String name) {
        super();
        this.setName(name);
        jointTransforms = new float[Constants.MAX_JOINTS_TRANSFORMS][16];
    }

    public void setSkeleton(Skeleton skeleton){
        this.skeleton = skeleton;
    }

    public Skeleton getSkeleton(){return skeleton;}

    public void setup(ShaderProgram sp, List<Mesh> meshes){
        setShaderProgramId(sp.getProgramHandle());
        for(Mesh m: meshes) {
            m.setupInterleavedMesh(sp);
        }
        setMesh(meshes);
    }

    private void updateRotation(Input input, int mode){
        if(mode == 0){
            return;
        }
        float xRot = input.getMovement()[0];
        float yRot = input.getMovement()[1];

        if(xRot != 0 || yRot != 0){
            setUpdateModelMatrix(true);
        }else{
            setUpdateModelMatrix(false);
        }

        //addAngularDisplacement(xRot,yRot,0);
    }
    
    private void updateRotation(float time) {
        float delta_t = time - getInitialRotTime();

        float angularDisplacement = getAngularVelocity() * (delta_t);
        //setAngularDisplacement(angularDisplacement);
        //when complete one cycle
        if (delta_t > 360 * (1 / getAngularVelocity())) {
            setInitialRotTime(time);
        }
    }

    @Override
    public void update(Timer timer,
                       Transformation transformation,
                       MousePicker mousePicker,
                       Input input,
                       Map<String, GameObject> gameObjectMap) {
        float time = timer.getDeltaTime();
        updateBonds(gameObjectMap);
        if(animation != null) {
            updateAnimation(time);
        }
        updateRotation(timer);
        updateModelMatrix(transformation);
    }

    private void updateAnimation(float time){
        if (animation == null) {
            return;
        }
        animation.increaseAnimationTime(time);
        Map<String, float[]> currentPose = animation.calculateCurrentAnimationPose();
        float[] tempMatrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        animation.applyPoseToJoints(currentPose, skeleton.getRootJoint(), tempMatrix, jointTransforms, skeleton);
        //animation.applyPoseToJoints2(currentPose, skeleton, jointTransforms);
    }

    @Override
    public void render(Scene scene, Transformation transformation, Camera camera){
        for(Mesh mesh: this.getMeshList()) {
            if(mesh == null) return; //do nothing

            int fromIndex = 0;
            int mIndex = 0;
            for(Material material: mesh.getMaterials()) {
                //create model view projection matrix
                float[] mMVPMatrix = transformation.getMVPMatrix(this.getModelMatrix());

                Light light = scene.getLight();
                ShaderProgram shaderProgram = scene.getShaderPrograms().get(getShaderProgramId());

                //sampler2did[0] is diffuse texture;
                material.setupSampler2d(0);

                //for now, there are only one int uniform to pass to shader program
                shaderProgram.passIntUniforms(DYNAMIC_INT_UNIFORMS,
                        new  int[]{material.getSample2Did()[0]});

                shaderProgram.passFloatUniforms(DYNAMIC_FLOAT_UNIFORMS,
                        transformation.getViewMatrix(), mMVPMatrix,
                        light.getLightPosInEyeSpace(), light.getLightColor(),
                        camera.getPosition().toFloat(), material.getEmission(),
                        light.getIntensity(),scene.getIorAmbient(),material.getRefraction());

                //pass animation joints transforms

                if(skeleton != null){
                    shaderProgram.passUniformMat4Array(UNIFORM_JT_MATRIX, jointTransforms);
                }

                //arithmetic progression: a_n = a_0 + n*r; a_0 = 0
                int toIndex = mesh.getIndices().length;

                //from the fromIndex index to the toIndex may consist of other material.
                mesh.render(GLES30.GL_TRIANGLES, toIndex, fromIndex * BYTES_PER_SHORT);

                fromIndex += toIndex;

               //mIndex++;
            }
        }
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

}
