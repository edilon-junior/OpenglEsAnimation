#version 300 es

uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_TexCoordinate;
layout(location = 2) in vec3 a_Normal;
layout(location = 3) in vec4 a_JointsId;
layout(location = 4) in vec4 a_Weights;

out vec3 v_Position;
out vec2 v_TexCoordinate;
out vec3 v_Normal;
out vec4 v_JointsId;
out vec4 v_Weights;

// The entry point for our vertex shader.
void main()
{
    // Transform the vertex into eye space.
    v_Position = vec3(u_MVMatrix * a_Position);

    // Pass through the texture coordinate.
    v_TexCoordinate = a_TexCoordinate;

    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

    v_JointsId = a_JointsId;

    v_Weights = a_Weights;

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = u_MVPMatrix * a_Position ;
}