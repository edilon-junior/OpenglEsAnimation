#version 300 es

uniform mat4 u_MVPMatrix;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec3 a_Normal;
layout(location = 2) in vec2 a_TexCoordinate;

out vec2 oi_TexCoordinate;

// The entry point for our vertex shader.
void main()
{
    oi_TexCoordinate = a_TexCoordinate;
    gl_Position = u_MVPMatrix * a_Position;
}