#version 300 es
precision mediump float;       	// Set the default precision to medium. We don't need as high of a

uniform vec3 u_LightPos;
uniform vec3 u_LightColor;
uniform vec3 u_ViewPos;

uniform vec3 u_Emission;
uniform sampler2D u_DiffuseMap;
uniform float u_Ambient;
uniform float u_IorAmbient;
uniform float u_Ior;

in vec3 oi_Position;
in vec2 oi_TexCoordinate;
in vec3 oi_Normal;
in vec4 oi_Color;

layout(location = 0) out vec4 o_FragColor;

//const float PI = 3.14159265359;
// SDP = 1/(sqrt(2*PI))
const float SDP = 0.3989422804;

//projection a over b
vec3 projection(vec3 a, vec3 b){
    float num = dot(a,b);
    float den = dot(b,b);
    return (num/den)*b;
}

void main()
{
    float lightDistance = length(u_LightPos - oi_Position.xyz);

    //ambient
    float ambient = u_Ambient;

    //diffuse
    vec3 norm = normalize(oi_Normal);
    vec3 lightDirection = normalize(u_LightPos - oi_Position);
    float diffuse = max(dot(norm, lightDirection), 0.0);
    //add attenuation
    diffuse = diffuse * (1.0 / (1.0 + (0.10 * lightDistance)));

    //specular
    float specularStrength = 1.0 - u_Ior;
    vec3 viewDir = normalize(u_ViewPos - oi_Position);
    vec3 reflectDir = reflect(-lightDirection, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 2.0);
    float specular = specularStrength * spec;

    vec3 color = u_LightColor * (ambient + diffuse + specular) + u_Emission;
    vec4 result = texture(u_DiffuseMap, oi_TexCoordinate)*vec4(color, 1.0);
    o_FragColor = result;
}
