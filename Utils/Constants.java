package com.example.openglexemple;

import com.example.openglexemple.Math.Vector3f;

public class Constants {
    public static final String ATTRIBUTE_POSITION = "a_Position";
    public static final String ATTRIBUTE_NORMAL = "a_Normal";
    public static final String ATTRIBUTE_TEXTURE_COORDINATE = "a_TexCoordinate";
    public static final String ATTRIBUTE_COLOR = "a_Color";
    public static final String ATTRIBUTE_WEIGHTS = "a_Weights";
    public static final String ATTRIBUTE_JOINTS_ID = "a_JointsId";
    public static final String UNIFORM_LIGHT_POS = "u_LightPos";
    public static final String UNIFORM_LIGHT_COLOR = "u_LightColor";
    public static final String UNIFORM_VIEW_POS = "u_ViewPos";
    public static final String UNIFORM_EMISSION = "u_Emission";
    public static final String UNIFORM_AMBIENT = "u_Ambient";
    public static final String UNIFORM_DIFFUSE = "u_Diffuse";
    public static final String UNIFORM_SPECULAR = "u_Specular";
    public static final String UNIFORM_SHININESS = "u_Shininess";
    public static final String UNIFORM_MV_MATRIX = "u_MVMatrix";
    public static final String UNIFORM_MVP_MATRIX = "u_MVPMatrix";
    public static final String UNIFORM_JT_MATRIX = "u_JointTransforms";
    public static final String UNIFORM_BS_MATRIX = "u_BindShapeMatrix";
    public static final String UNIFORM_TEXTURE = "u_Texture";
    public static final String UNIFORM_COLOR = "u_Color";
    public static final String UNIFORM_POINT_SIZE = "u_PointSize";
    public static final String UNIFORM_IOR_AMBIENT = "u_IorAmbient";
    public static final String UNIFORM_IOR = "u_Ior";
    public static final String UNIFORM_DIFFUSE_MAP = "u_DiffuseMap";
    public static final String UNIFORM_SELECTED = "u_Selected";
    public static final int POSITION_SIZE = 3;
    public static final int NORMAL_SIZE = 3;
    public static final int TEXTURE_COORDINATE_SIZE = 2;
    public static final int WEIGHTS_SIZE = 4;
    public static final int BONES_IDS_SIZE = 4;
    public static final int MAX_INTERACTIONS = 4;
    public static final int COLOR_SIZE = 4;
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_INT = 4;
    public static final int BYTES_PER_SHORT = 2;
    public static final int MAX_JOINTS_TRANSFORMS = 50;
    public static final int STRIDE_T = (POSITION_SIZE + NORMAL_SIZE + TEXTURE_COORDINATE_SIZE) * BYTES_PER_FLOAT;
    public static final int STRIDE_C = (POSITION_SIZE + NORMAL_SIZE + COLOR_SIZE) * BYTES_PER_FLOAT;
    public static final int STRIDE_TC = (POSITION_SIZE + NORMAL_SIZE + TEXTURE_COORDINATE_SIZE + COLOR_SIZE) * BYTES_PER_FLOAT;
    public static final int STRIDE_WB = (POSITION_SIZE +
            NORMAL_SIZE + TEXTURE_COORDINATE_SIZE + WEIGHTS_SIZE + BONES_IDS_SIZE) * BYTES_PER_FLOAT;
    public static final Vector3f GRAVITY = new Vector3f(0, -10, 0);
    public static final byte TYPE_FLOAT = -128;
    public static final byte TYPE_INT = -127;
    public static final byte TYPE_BOOLEAN = -126;
    public static final byte TYPE_FLOAT_ARRAY = -125;
    public static final byte TYPE_BYTE = -124;
    public static final byte INFO_POSITION_CHANGE = -11;
    public static final byte INFO_POSITION= -10;
    public static final byte INFO_ROTATION= -9;
    public static final byte INFO_AIMED = -8;
    public static final byte INFO_TOUCH_COUNTER = -7;
    public static final byte INFO_ROTPARAM = -6;
    public static final byte INFO_SELECTED = -5;
    public static final byte INFO_TOUCHED = -4;
    public static final byte INFO_ROTATION_AXIS = -3;
    public static final byte INFO_MODEL_MATRIX = 1;
    public static final byte DO_ROTATE = 2;
    public static final byte DO_TRANSLATE = 3;
    //must be the same order as in vertex shader file
    public static final String[] PLY_ATTRIBUTES = new String[]{
            ATTRIBUTE_POSITION, ATTRIBUTE_NORMAL,
            ATTRIBUTE_TEXTURE_COORDINATE, ATTRIBUTE_COLOR
    };

    public static final String[] OBJ_ATTRIBUTES = new String[]{
            ATTRIBUTE_POSITION, ATTRIBUTE_NORMAL,
            ATTRIBUTE_TEXTURE_COORDINATE
    };
    public static final String[] BUTTON_INT_UNIFORMS = new String[]{
            UNIFORM_TEXTURE, UNIFORM_SELECTED
    };
    public static final String[] BUTTON_FLOAT_UNIFORMS = new String[]{
            UNIFORM_MVP_MATRIX
    };
    public static final String[] STATIC_FLOAT_UNIFORMS = new String[]{
            UNIFORM_LIGHT_POS, UNIFORM_LIGHT_COLOR, UNIFORM_VIEW_POS,
            UNIFORM_AMBIENT, UNIFORM_DIFFUSE, UNIFORM_SPECULAR, UNIFORM_SHININESS,
            UNIFORM_MV_MATRIX, UNIFORM_MVP_MATRIX
    };

    public static final String[] STATIC_INT_UNIFORMS = new String[]{
            UNIFORM_TEXTURE
    };
    public static final String[] DYNAMIC_FLOAT_UNIFORMS = new String[]{
            UNIFORM_MV_MATRIX, UNIFORM_MVP_MATRIX,
            UNIFORM_LIGHT_POS, UNIFORM_LIGHT_COLOR, UNIFORM_VIEW_POS,
            UNIFORM_EMISSION, UNIFORM_IOR_AMBIENT, UNIFORM_IOR, UNIFORM_BS_MATRIX
    };

    public static final String[] DYNAMIC_INT_UNIFORMS = new String[]{
            UNIFORM_DIFFUSE_MAP
    };
    public static final String[] DYNAMIC_ATTRIBUTES = new String[]{
            ATTRIBUTE_POSITION, ATTRIBUTE_NORMAL, ATTRIBUTE_TEXTURE_COORDINATE, ATTRIBUTE_COLOR,
            ATTRIBUTE_JOINTS_ID, ATTRIBUTE_WEIGHTS
    };

    public static final String[] DYNAMIC_MAT4ARR_UNIFORMS = {UNIFORM_JT_MATRIX};
    public static int[] SCENE_ATTRIBUTES_SIZES = new int[]{
            POSITION_SIZE, NORMAL_SIZE, TEXTURE_COORDINATE_SIZE, COLOR_SIZE
    };

    public static final String[] POINT_ATTRIBUTES = new String[] {ATTRIBUTE_POSITION};
    public static final String[] POINT_INT_UNIFORMS = new String[]{UNIFORM_SELECTED};
    public static final String[] POINT_FLOAT_UNIFORMS = new String[]{UNIFORM_MVP_MATRIX, UNIFORM_COLOR, UNIFORM_POINT_SIZE};

    public static int getStriderJW(int max_interactions){
        return (POSITION_SIZE + NORMAL_SIZE + TEXTURE_COORDINATE_SIZE +
                max_interactions * 2) * BYTES_PER_FLOAT;
    }
    public static int getStriderPNTCJW(int max_interactions){
        return (POSITION_SIZE + NORMAL_SIZE + TEXTURE_COORDINATE_SIZE + COLOR_SIZE +
                max_interactions * 2) * BYTES_PER_FLOAT;
    }

    public static float DEGREE_TO_RAD = (float) (Math.PI/180);

}
