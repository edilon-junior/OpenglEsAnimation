#version 300 es
precision mediump float;

uniform int u_Selected;

in vec4 v_Color;
layout(location = 0) out vec4 o_FragColor;

void main()
{
    if(u_Selected != 1000){
        o_FragColor = v_Color;
    }
    //if(u_Selected == 0) {
        o_FragColor = v_Color;
    //}else{
      //  o_FragColor = v_Color * vec4(0.0);
    //}
}