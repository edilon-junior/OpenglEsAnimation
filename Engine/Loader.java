package com.example.openglexemple.Engine;

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
import com.example.openglexemple.GameObjects.DynamicModel;
import com.example.openglexemple.GameObjects.Button;
import com.example.openglexemple.ColladaParser.ColladaDataHandler;
import com.example.openglexemple.GraphicObjects.Material;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.PolygonParser;
import Saver;
import com.example.openglexemple.GameObjects.SolidModel;
import com.example.openglexemple.Utils;
import com.example.openglexemple.WavefrontParser;
import com.example.openglexemple.XmlParser.XmlNode;
import com.example.openglexemple.XmlParser.XmlParser;

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
        XmlNode libraryGeometry = colladaData.getChild("library_geometries");
        dynamicModels = new DynamicModel[libraryGeometry.getChildren("geometry").size()];

        XmlNode libraryVisualScenes = colladaData.getChild("library_visual_scenes");
        XmlNode libraryEffects = colladaData.getChild("library_effects");
        XmlNode libraryImages = colladaData.getChild("library_images");
        XmlNode libraryMaterial = colladaData.getChild("library_materials");
        XmlNode libraryGeometries = colladaData.getChild("library_geometries");
        XmlNode libraryControllers = colladaData.getChild("library_controllers");
        XmlNode libraryAnimations = colladaData.getChild("library_animations");

        XmlNode scene0 = libraryVisualScenes.getChild("visual_scene");
        List<XmlNode> scene0Children = scene0.getChildren("node");

        int dmCounter = 0;
        for (XmlNode node : scene0Children) {
            System.out.println(TAG + " try create gameObject: " + node.getAttribute("id"));

            DynamicModel dynamicModel = new DynamicModel(node.getAttribute("id"));
            XmlNode instanceGeometryNode = node.getChild("instance_geometry");

            if(instanceGeometryNode == null){
                //armature, camera and light objects
                continue;
            }

            String instanceGeometryUrl = instanceGeometryNode.getAttribute("url").substring(1);
            XmlNode instanceController = node.getChild("instance_controller");
            String instanceControllerUrl;
            XmlNode geometryNode = libraryGeometries.getChildWithAttribute("geometry", "id", instanceGeometryUrl);
            XmlNode controllerNode;
            XmlNode skin;
            XmlNode armatureNode;
            ColladaDataHandler colladaDataHandler = new ColladaDataHandler(node, geometryNode);
            float[] transposeBSM = new float[16];
            if(instanceController != null) {
                instanceControllerUrl = instanceController.getAttribute("url").substring(1);
                controllerNode = libraryControllers.getChildWithAttribute("controller","id",instanceControllerUrl);
                skin = controllerNode.getChild("skin");
                armatureNode = scene0.getChildWithAttribute("node","name",controllerNode.getAttribute("name"));

                //create skeleton---------------------------
                colladaDataHandler.setSkin(skin);
                colladaDataHandler.setArmature(armatureNode);
                colladaDataHandler.createSkeleton();
                //create animations -----------------------
                XmlNode mainAnimationNode =libraryAnimations.getChildWithAttribute("animation","name",controllerNode.getAttribute("name"));
                float[] bindShapeMatrix = Utils.stringToFloatArray(skin.getChild("bind_shape_matrix").getData());
                Animation mainAnimation = new Animation(mainAnimationNode, colladaDataHandler.getSkeleton().getRootJointId());
                //transpose bind shape matrix
                Matrix.transposeM(transposeBSM, 0, bindShapeMatrix, 0);
                mainAnimation.setBindShapeMatrix(transposeBSM);
                colladaDataHandler.setBindShapeMatrix(transposeBSM);
                //-------------------- setup animation and skeleton ------------------------------
                dynamicModel.setSkeleton(colladaDataHandler.getSkeleton());
                dynamicModel.setAnimation(mainAnimation);
            }
            dynamicModel.fillJointTransforms();
            //create mesh-------------------------------
            Mesh mesh = colladaDataHandler.createMesh(transposeBSM);
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            Saver saver = new Saver(mesh.getName());
            saver.addVertices(mesh.getVertices(), mesh.getSemantics(), mesh.getVertexStrider());
            saver.addIndices(mesh.getIndices());
            saver.writeFileOnInternalStorage(context);
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

            //create material---------------------------
            String materialTarget = instanceGeometryNode.getChild("bind_material")
                    .getChild("technique_common")
                    .getChild("instance_material")
                    .getAttribute("target").substring(1);
            XmlNode materialNode = libraryMaterial.getChildWithAttribute("material", "id", materialTarget);
            String effectUrl = materialNode.getChild("instance_effect").getAttribute("url").substring(1);
            XmlNode effectNode = libraryEffects.getChildWithAttribute("effect", "id", effectUrl);

            Material material = colladaDataHandler.createMaterial(materialNode, effectNode);
            //load texture ----------------------------
            //get image file name
            String imageFileName = libraryImages.getChildWithAttribute("image", "id", material.getDiffuseMap()).getChild("init_from").getData();

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
