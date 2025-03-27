package com.example.openglexemple.Engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;

import com.example.openglexemple.GameObjects.GameObject;
import com.example.openglexemple.GraphicObjects.Scene;

public class MyRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = "RENDERER";
    private final int FRAMES_PER_SECOND = 30;
    private final int MILLISECONDS_PER_FRAME = 120 / FRAMES_PER_SECOND;
    private final int MAX_FRAMESKIP = 24;

    long lastLoopTime;
    int loops;
    private final Context mActivityContext = null;
    private final Transformation transformation;
    private final Loader loader;
    private Scene scene;
    float initial_time = 0;
    boolean start = false;
    private Input input;
    private Timer timer;
    int windowWidth;
    int windowHeght;
    private MousePicker mousePicker;
    public MyRenderer(final Context activityContext)
    {
        ///mActivityContext = activityContext;
        DisplayMetrics metrics = activityContext.getResources().getDisplayMetrics();
        windowWidth = metrics.widthPixels;
        windowHeght = metrics.heightPixels;
        loader = new Loader(activityContext);
        transformation = new Transformation();
        loops = 0;
        lastLoopTime = System.currentTimeMillis();
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        input.setDisplaySize(new int[]{windowWidth, windowHeght});
        scene = new Scene(loader, transformation);

        scene.createGameObjects(loader);

        // Set the background clear color.
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

        GLES30.glLineWidth(4);

        // Use culling to remove back faces.
        enableCulling();
        // Enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        //GLES30.glFrontFace(GLES30.GL_CCW);
        transformation.createPerspectiveMatrix(windowWidth, windowHeght);
        transformation.createViewMatrix();
        mousePicker = new MousePicker(scene.getCamera());
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        windowWidth = width;
        windowHeght = height;
        // Set the OpenGL viewport to the same size as the surface.
        GLES30.glViewport(0, 0, width, height);

        input.setDisplaySize(new int[]{width,height});
        transformation.createPerspectiveMatrix(width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        loops = 0;

        while(System.currentTimeMillis() > lastLoopTime &&
                loops < MAX_FRAMESKIP) {

            scene.getCamera().update(input);
            transformation.updateViewMatrix(scene.getCamera());
            transformation.createViewProjectionMatrix();

            if(input.isTouch()){
                mousePicker.update(transformation.getViewMatrix(), transformation.getProjectionMatrix(), input);
                System.out.println(TAG + " ----------------------------------------------");
                System.out.println(TAG+" mouse picker line "+mousePicker.getLine().toString());
            }

            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

            timer.update();

            //System.out.println(TAG+" MyRenderer delta time: "+timer.getDeltaTime());
            scene.getButtonProgram().start();
            for(GameObject button : scene.getButtonsList()){
                button.prepare(timer);
                button.update(timer, transformation, mousePicker, input, scene.getGameObjectsMap());
                button.render(scene, transformation);
                button.cleanup();
                scene.getGameObjectsMap().put(button.getName(), button);
            }
            scene.getButtonProgram().stop();

            scene.getPointProgram().start();
            for (GameObject point : scene.getPointList()) {
                point.update(timer, transformation, mousePicker, input,scene.getGameObjectsMap());
                point.render(scene.getPointProgram(), transformation);
                scene.getGameObjectsMap().put(point.getName(), point);
            }
            scene.getPointProgram().stop();

            scene.getObjProgram().start();
            for (GameObject solidModel : scene.getSolidModelList()) {
                solidModel.update(timer, transformation, mousePicker, input);
                solidModel.render(scene, transformation);
                scene.getGameObjectsMap().put(solidModel.getName(), solidModel);
            }
            scene.getObjProgram().stop();

            scene.getPlyProgram().start();
            for (GameObject plyModel : scene.getPlyModelList()) {
                plyModel.update(timer);
                plyModel.render(scene, transformation);
                scene.getGameObjectsMap().put(plyModel.getName(), plyModel);
            }
            scene.getPlyProgram().stop();

            scene.getDynamicProgram().start();
            for (GameObject dynamicModel : scene.getDynamicModelList()) {
                dynamicModel.update(timer, transformation, mousePicker, input,scene.getGameObjectsMap());
                dynamicModel.render(scene, transformation, scene.getCamera());
                scene.getGameObjectsMap().put(dynamicModel.getName(), dynamicModel);
            }

            if(input.isTouch()){
                System.out.println(TAG+" touch!");
            }

            scene.getDynamicProgram().stop();
            input.cleanup();
            lastLoopTime += MILLISECONDS_PER_FRAME;
            loops++;
        }
    }

    private void enableCulling(){
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);
    }
    private void disableCulling(){
        GLES30.glDisable(GLES30.GL_CULL_FACE);
    }

    public void setInput(Input input) {this.input = input;
    }
    
    public void setTimer(Timer timer){
        this.timer = timer;
    }
}
