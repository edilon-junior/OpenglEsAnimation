package com.example.openglexemple.Loader;

import static android.opengl.GLES20.GL_TEXTURE_2D;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.openglexemple.Animation.Animation;
import com.example.openglexemple.Animation.Skeleton;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.GameObjects.DynamicModel;
import com.example.openglexemple.GameObjects.Button;
import com.example.openglexemple.Loader.ColladaParser.AnimationLoader;
import com.example.openglexemple.Loader.ColladaParser.ColladaDataHandler;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;

import com.example.openglexemple.Loader.ColladaParser.ControllerLoader;
import com.example.openglexemple.Loader.ColladaParser.GeometryLoader;
import com.example.openglexemple.Loader.ColladaParser.MaterialLoader;
import com.example.openglexemple.Loader.ColladaParser.SkeletonLoader;
import com.example.openglexemple.Utils.Saver;
import com.example.openglexemple.GameObjects.SolidModel;
import com.example.openglexemple.Utils.Utils;
import com.example.openglexemple.Loader.XmlParser.XmlNode;
import com.example.openglexemple.Loader.XmlParser.XmlParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Loader {
    private static final String TAG = "LOADER";
    private final Context context;

    public Loader(Context context) {
        this.context = context;
    }

    public Bitmap loadAssetBitmap(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        AssetManager assetManager =  context.getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream == null){
            System.out.println("error to load bitmap from "+ path);
        }

        Bitmap bitmap_asset = BitmapFactory.decodeStream(inputStream, null, options);

        //flip image on y axis
        android.graphics.Matrix flip = new android.graphics.Matrix();
        flip.postScale(1f, -1f);

        if (bitmap_asset != null) {
            return Bitmap.createBitmap(bitmap_asset, 0, 0, bitmap_asset.getWidth(), bitmap_asset.getHeight(), flip, true);
        }
        return null;
    }
    public int loadTexture(final String modelName, final String textureName) {
        if(textureName.isEmpty()){
            return 0;
        }

        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error generating texture from "+modelName);
        }

        final Bitmap bitmap = loadAssetBitmap("models/"+ modelName+"/"+textureName);

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        //GLES30.glTexImage2D(GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES30.GL_RGBA,GLES30.GL_UNSIGNED_BYTE,bitmap.getP);
        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        System.out.println(TAG+" loaded texture handle: "+textureHandle[0]);
        return textureHandle[0];
    }

    public SolidModel[] loadSolidModels(String fileName, ShaderProgram shaderProgram){
        SolidModel[] solidModels = null;
        Mesh[] solidMeshes = null;
        String[] fileNameSplitted = fileName.split("\\.");

        if(fileNameSplitted.length < 2){
            System.out.println(TAG+" missing file name extension");
            return new SolidModel[0];
        }
        String modelName = fileNameSplitted[0];
        String extension = fileNameSplitted[1];

        System.out.println(TAG+" model file name: "+ modelName +"; extension: "+extension);

        int type = -1;
        if(extension.equalsIgnoreCase("obj")){
            solidMeshes = loadMeshOBJ(modelName);
            type = 0;
        }
        if(extension.equalsIgnoreCase("ply")){
            solidMeshes = new Mesh[]{loadMeshPly(modelName)};
            type = 0;
        }
        if(type == -1){
            System.out.println(TAG+" error of extension in "+fileName);
            return null;
        }

        if(solidMeshes != null) {
            solidModels = new SolidModel[solidMeshes.length];
            for (int i = 0 ; i< solidMeshes.length; i++) {
                Mesh mesh = solidMeshes[i];

                if (type == 0) {
                    mesh.setupInterleavedMesh(shaderProgram);
                } else if (type == 1) {
                    mesh.setupNonInterleavedMesh(shaderProgram);
                } else if (type == 2) {
                    mesh.createBuffers();
                }
                SolidModel solidModel = new SolidModel(fileName+"_"+mesh.getName(), shaderProgram );
                List<Mesh> meshList = new ArrayList<>();
                meshList.add(mesh);
                solidModel.setMesh(meshList);
                solidModels[i] = solidModel;
            }
        }
        return solidModels;
    }

    public Mesh[] loadMeshOBJ(String modelName) {
        AssetManager assetManager = context.getAssets();
        WavefrontParser wavefrontParser = new WavefrontParser(assetManager, modelName);

        ArrayList<Mesh> meshLibrary = wavefrontParser.getMeshLibrary();
        Map<String, Material> materialLibrary = wavefrontParser.getMaterialLibrary();

        //set material to each mesh
        for(Mesh mesh : meshLibrary) {
            Material material = materialLibrary.get(mesh.getMaterialId());

            if(material == null){
                material = new Material();
            }else {
                System.out.println(TAG+" set material "+material.getId()+" to mesh "+mesh.getName());
                String textureFileName = material.getTextureFileName();
                int textureId = loadTexture(modelName, textureFileName);
                material.setSample2Did(new int[]{textureId});
            }
            mesh.setMaterials(new Material[]{material});
        }

        Mesh[] meshes = new Mesh[meshLibrary.size()];
        return meshLibrary.toArray(meshes);
    }
    /**
     * load mesh from ply (polygon file format)
     */
    public Mesh loadMeshPly(String modelName) {
        AssetManager assetManager = context.getAssets();
        PolygonParser polygonParser = new PolygonParser(assetManager, modelName);
        Mesh mesh = polygonParser.getMesh();

        for(Material material: mesh.getMaterials()) {
            int[] sampler2d;
            if (material.getTextureFileName().isEmpty()) {
                sampler2d = Utils.createFakeTexture();
            } else {
                sampler2d = new int[]{loadTexture(modelName, material.getTextureFileName())};
            }
           material.setSample2Did(sampler2d);
        }
        return mesh;
    }

    public String loadShader(String shaderName) {

        BufferedReader bufferedReader;
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("shaders/" + shaderName), StandardCharsets.UTF_8));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.wtf(TAG, "can not load shader "+shaderName);
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public DynamicModel[] loadDAE(String modelName, ShaderProgram shaderProgram) {

        DynamicModel[] dynamicModels = null;

        AssetManager assetManager = context.getAssets();
        BufferedReader bufferedReader = null;
        XmlNode colladaData = null;

        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(assetManager.open(
                            "models/" + modelName + "/" + modelName + ".dae")));
            colladaData = XmlParser.loadXmlFile(bufferedReader);
            bufferedReader.close();
        } catch (IOException e) {
            Log.wtf(TAG, "can not load xml file"+ modelName);
            e.printStackTrace();
        }

        //############ PROCESS COLLADA DATA #####################
        assert colladaData != null: "error to create gameObject "+modelName;

        XmlNode libraryVisualScenes = colladaData.getChild("library_visual_scenes");

        XmlNode scene0 = libraryVisualScenes.getChild("visual_scene");
        List<XmlNode> scene0Children = scene0.getChildren("node");
        dynamicModels = new DynamicModel[scene0Children.size()];
        int dmCounter = 0;

        for (XmlNode node : scene0Children) {
            System.out.println(TAG + " try create gameObject: " + node.getAttribute("id"));

            DynamicModel dynamicModel = new DynamicModel(node.getAttribute("id"));

            ColladaDataHandler colladaDataHandler = new ColladaDataHandler(node, colladaData);

            XmlNode armature = colladaDataHandler.getArmatureNode();
            XmlNode meshNode = colladaDataHandler.getMeshNode();
            XmlNode controller = colladaDataHandler.getController();
            XmlNode animationNode = colladaDataHandler.getAnimation();
            Skeleton skeleton = null;
            Animation animation = null;
            float[][] jointsId = null;
            float[][] weights = null;
            float[] bindShapeMatrix = null;
            //create skeleton and animation ---------------
            if( controller != null) {
                ControllerLoader controllerLoader = new ControllerLoader(controller);
                jointsId = controllerLoader.getJointsId();
                weights = controllerLoader.getVertexWeights();
                bindShapeMatrix = controllerLoader.getBindShapeMatrix();
                SkeletonLoader skeletonLoader = new SkeletonLoader(armature,
                        meshNode, controllerLoader.getIbmMap(), controllerLoader.getJointIndices());
                skeletonLoader.createSkeleton();
                skeleton = skeletonLoader.getSkeleton();
                AnimationLoader animationLoader = new AnimationLoader(animationNode, skeleton.getRootJointId());
                animation = animationLoader.createAnimation();
            }
            dynamicModel.setSkeleton(skeleton);
            dynamicModel.setAnimation(animation);
            //create mesh-------------------------------
            XmlNode geometry = colladaDataHandler.getGeometry();
            GeometryLoader geometryLoader = new GeometryLoader(geometry, jointsId, weights, bindShapeMatrix);
            Mesh mesh = geometryLoader.createMesh();
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/*
            Saver saver = new Saver(mesh.getName());
            saver.addVertices(mesh.getVertices(), mesh.getSemantics(), mesh.getVertexStrider());
            saver.addIndices(mesh.getIndices());
            saver.writeFileOnInternalStorage(context);
*/
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

            //create material---------------------------
            MaterialLoader materialLoader = new MaterialLoader(colladaDataHandler.getMaterialNode(), colladaDataHandler.getEffect());
            Material material = materialLoader.createMaterial();
            String imageFileName = colladaDataHandler.findImage(material.getDiffuseMap());
            //load texture ----------------------------
            if (material.getDiffuseMap() != null) {
                int diffuseId = loadTexture(modelName, imageFileName );
                int[] texturesIds = new int[]{diffuseId};
                //---------------------- add texture to material ----------------------
                material.setSample2Did(texturesIds);
            }
            mesh.setMaterials(new Material[]{material});

            //-------------------- set mesh to dynamic model and setup shader----------
            List<Mesh> meshes = new ArrayList<>();
            meshes.add(mesh);
            //set mesh to model
            dynamicModel.setup(shaderProgram, meshes);

            dynamicModels[dmCounter] = dynamicModel;
            dmCounter++;
            meshes.clear();
        }

        System.out.println(TAG + " sucess to load " + modelName + " model components size: " + dynamicModels.length);

        return dynamicModels;
    }

    public Button loadButton(String name, String textureName, ShaderProgram shaderProgram){
        Button button = new Button(shaderProgram);
        button.setName(name);
        int sampler2d = loadTexture("buttons", textureName);
        Material material = new Material();
        material.setSample2Did(new int[]{sampler2d});
        Material[] materials = new Material[]{material};
        button.getMesh(0).setMaterials(materials);
        return button;
    }
}
