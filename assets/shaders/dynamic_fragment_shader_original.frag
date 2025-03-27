#version 300 es
precision mediump float;       	// Set the default precision to medium. We don't need as high of a

uniform vec3 u_LightPos;
uniform vec3 u_LightColor;
uniform vec3 u_ViewPos;
uniform sampler2D u_DiffuseMap;

uniform vec3 u_Ambient;
uniform vec3 u_Diffuse;
uniform vec3 u_Specular;
uniform float u_Shininess;

in vec3 v_Position;
in vec2 v_TexCoordinate;
in vec3 v_Normal;
in vec4 v_JointsId;
in vec4 v_Weights;

layout(location = 0) out vec4 o_FragColor;

void main()
{
    //ambient
    vec3 ambient = u_Ambient;

    //diffuse
    vec3 norm = normalize(v_Normal);
    float distance = length(u_LightPos - v_Position);
    vec3 lightDirection = normalize(u_LightPos - v_Position);
    float diff = max(dot(norm, lightDirection), 0.0);
    vec3 diffuse =  diff * (1.0 / distance) * u_Diffuse;

    //specular
    vec3 viewDir = normalize(u_ViewPos - v_Position);
    vec3 reflectDir = reflect(-lightDirection, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), u_Shininess);
    vec3 specular = spec * u_Specular;

    vec3 result = u_LightColor * (ambient + diffuse + specular);

    //o_FragColor = vec4(result,1.0) * texture(u_Texture, v_TexCoordinate);
    o_FragColor = texture(u_DiffuseMap, v_TexCoordinate);

}