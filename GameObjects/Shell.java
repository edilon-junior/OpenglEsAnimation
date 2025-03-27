package com.example.openglexemple.GameObjects;

import com.example.openglexemple.Engine.Input;
import com.example.openglexemple.Engine.Loader;
import com.example.openglexemple.Engine.MousePicker;
import com.example.openglexemple.Engine.ShaderProgram;
import com.example.openglexemple.Engine.Transformation;
import com.example.openglexemple.GraphicObjects.Mesh;
import com.example.openglexemple.Math.PlaneEquation;
import com.example.openglexemple.Math.Vector3f;
import com.example.openglexemple.Engine.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Shell extends GameObject {

    //private final PlaneEquation[] planes;

    public Shell(Mesh mesh, Loader loader, ShaderProgram shaderProgram){
        super();
        List<Mesh> meshes = new ArrayList<>();
        meshes.add(mesh);
        setMesh(meshes);
        //planes = createPlanes(shellMesh);
    }

    /**
     *
     * @param mesh
     * @param type
     *          0 2d circle shell
     *          1 3d sphere shell
     * @param edges
     * @return
     */
    private PlaneEquation[] createPlanes(Mesh mesh, int type, int edges){
        short[] indices = mesh.getIndices();
        float[] positions = mesh.getPositions();
        PlaneEquation[] planes = new PlaneEquation[indices.length / 3];

        if(type == 0){

        }
        if(type == 1){
            for(int i = 0; i < indices.length / 3;i++) {
                float v1x = positions[(indices[i * 3]) * 3];
                float v1y = positions[(indices[i * 3]) * 3 + 1];
                float v1z = positions[(indices[i * 3]) * 3 + 2];
                Vector3f vertex1 = new Vector3f(v1x, v1y, v1z);
                float v2x = positions[(indices[i * 3 + 1]) * 3];
                float v2y = positions[(indices[i * 3 + 1]) * 3 + 1];
                float v2z = positions[(indices[i * 3 + 1]) * 3 + 2];
                Vector3f vertex2 = new Vector3f(v2x, v2y, v2z);
                float v3x = positions[(indices[i * 3 + 2]) * 3];
                float v3y = positions[(indices[i * 3 + 2]) * 3 + 1];
                float v3z = positions[(indices[i * 3 + 2]) * 3 + 2];
                Vector3f vertex3 = new Vector3f(v3x, v3y, v3z);
                if (i % 3 == 0) {
                    planes[i / 3] = new PlaneEquation(vertex1, vertex2, vertex3);
                }
            }
            //add only unique equations
            int length = planes.length;
            for (int i = 0; i < planes.length; i++) {
                for (int j = 0; j < planes.length; j++) {
                    if (i == j) continue;
                    if (i < j) {
                        if (planes[i].isEquivalent(planes[j])) {
                            planes[j] = null;
                            length -= 1;
                        }
                    }
                }
            }
            PlaneEquation[] uniquePlanes = new PlaneEquation[length];
            int j = 0;
            for (int i = 0; i < uniquePlanes.length; i++) {
                if (planes[i] != null) {
                    uniquePlanes[j] = planes[j];
                    j++;
                }
            }
            return uniquePlanes;
        }
        return planes;
    }

    @Override
    public void update(Timer timer, Transformation transformation, MousePicker mousePicker, Input input, Map<String, GameObject> gameObjectMap) {

    }
}
