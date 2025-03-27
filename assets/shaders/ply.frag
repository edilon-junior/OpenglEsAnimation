#version 300 es
precision mediump float;       	// Set the default precision to medium. We don't need as high of a

uniform vec3 u_LightPos;
uniform vec3 u_LightColor;
uniform vec3 u_ViewPos;
uniform sampler2D u_Texture;

uniform vec3 u_Ambient;
uniform vec3 u_Diffuse;
uniform vec3 u_Specular;
uniform float u_Shininess;

in vec3 v_Position;
in vec2 v_TexCoordinate;
in vec3 v_Normal;
in vec4 v_Color;

layout(location = 0) out vec4 o_FragColor;

void main()
{
    vec3 ambient = u_LightColor * u_Ambient;

    vec3 norm = normalize(v_Normal);

    float distance = length(u_LightPos - v_Position);

    vec3 lightDirection = normalize(u_LightPos - v_Position);

    float diff = max(dot(norm, lightDirection), 0.0);
    vec3 diffuse =  u_Diffuse * diff * (1.0 / distance);

    vec3 viewDir = normalize(u_ViewPos - v_Position);
    vec3 reflectDir = reflect(-lightDirection, norm);

    float spec = pow(max(dot(viewDir, reflectDir), 0.0), u_Shininess);
    vec3 specular = u_LightColor * (spec * u_Specular);

    vec3 sum = ambient + diffuse + specular;

    vec4 result = vec4(sum,1.0) * texture(u_Texture, v_TexCoordinate) + v_Color;

    o_FragColor = result;
}