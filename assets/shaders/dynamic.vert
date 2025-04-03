#version 300 es

const int MAX_JOINTS = 50;//max joints allowed in a skeleton
const int MAX_WEIGHTS = 4;//max number of joints that can affect a vertex

uniform mat4 u_JointTransforms[MAX_JOINTS];
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec4 a_Normal;
layout(location = 2) in vec2 a_TexCoordinate;
layout(location = 3) in vec4 a_Color;
layout(location = 4) in vec4 a_JointsId;
layout(location = 5) in vec4 a_Weights;

out vec3 oi_Position;
out vec2 oi_TexCoordinate;
out vec3 oi_Normal;
out vec4 oi_Color;

vec4 multiply(mat4 m, vec4 v){
    vec4 res;
    res.x = m[0][0]*v.x + m[1][0]*v.y + m[2][0]*v.z + m[3][0]*v.w;
    res.y = m[0][1]*v.x + m[1][1]*v.y + m[2][1]*v.z + m[3][1]*v.w;
    res.z = m[0][2]*v.x + m[1][2]*v.y + m[2][2]*v.z + m[3][2]*v.w;
    res.w = m[0][3]*v.x + m[1][3]*v.y + m[2][3]*v.z + m[3][3]*v.w;
    return res;
}

/*
vec4 multiply(mat4 m, vec3 v){
    vec4 res;
    res.x = m[0][0]*v.x + m[1][0]*v.y + m[2][0]*v.z + m[3][0]*1.0;
    res.y = m[0][1]*v.x + m[1][1]*v.y + m[2][1]*v.z + m[3][1]*1.0;
    res.z = m[0][2]*v.x + m[1][2]*v.y + m[2][2]*v.z + m[3][2]*1.0;
    res.w = m[0][3]*v.x + m[1][3]*v.y + m[2][3]*v.z + m[3][3]*1.0;
    return res;
}
*/
void main()
{
    mat4 skinMat = mat4(0.0);
    //vec4 totalPos = vec4(0.0);
    //vec4 totalNor = vec4(0.0);
    vec4 test = vec4(0.01);
    bool hasJoints = false;

    for(int i=0; i < MAX_WEIGHTS; i++) {
        int index = int(a_JointsId[i]);

        if(index == -1){
            continue;
        }
        hasJoints = true;
        mat4 jointTransform = u_JointTransforms[index];
        skinMat += jointTransform * a_Weights[i];
        //totalPos += jointTransform * a_Position * a_Weights[i];
        //totalNor += jointTransform * a_Normal * a_Weights[i];
    }

    if(hasJoints == false){
        skinMat = mat4(1.0);
    }
    //transform position into eye space
    vec4 animPos;
    vec4 animNor;

    animPos = skinMat * a_Position;
    animNor = skinMat * a_Normal;

    oi_Position = vec3(u_MVMatrix * animPos);
    //oi_Position = vec3(u_MVMatrix * totalPos);

    // Transform the normal's orientation into eye space.
    //oi_Normal = vec3(u_MVMatrix * totalNor);
    oi_Normal = vec3(u_MVMatrix * animNor);

    // Pass through the texture coordinate.
    oi_TexCoordinate = a_TexCoordinate;

    oi_Color = a_Color * test;

    gl_Position = u_MVPMatrix * animPos;
    //gl_Position = u_MVPMatrix * totalPos;
}
