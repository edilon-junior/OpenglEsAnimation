#version 300 es
precision mediump float;

uniform mat4 u_MVPMatrix;
uniform vec4 u_Color;
uniform float u_PointSize;

layout(location=0) in vec4 a_Position;

out vec4 v_Color;

void main()
{
    v_Color = u_Color;

    gl_Position = u_MVPMatrix * a_Position;
    gl_PointSize = u_PointSize;
}