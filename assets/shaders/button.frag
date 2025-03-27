#version 300 es
precision mediump float;       	// Set the default precision to medium. We don't need as high of a

uniform sampler2D u_Texture;
uniform int u_Selected;

in vec2 oi_TexCoordinate;

layout(location = 0) out vec4 o_FragColor;

void main()
{
    vec4 selectedFactor = vec4(1.0)*float(u_Selected);
    vec4 textureColor = texture(u_Texture, oi_TexCoordinate);
    if(textureColor.a < 0.5){
        discard;
    }

    o_FragColor = textureColor+selectedFactor;
}