#version 300 es
precision mediump float;       	// Set the default precision to medium. We don't need as high of a

uniform vec3 u_LightPos;
uniform vec3 u_LightColor;
uniform vec3 u_ViewPos;

uniform vec3 u_Emission;
uniform sampler2D u_DiffuseMap;
uniform float u_Ior;
uniform float u_IorAmbient;

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
    // ambient
    vec3 ambient = u_LightColor * texture(u_DiffuseMap, oi_TexCoordinate).rgb;

    //diffuse
    vec3 norm = normalize(oi_Normal);
    vec3 lightDirection = normalize(u_LightPos - oi_Position);
    float diff = max(dot(norm, lightDirection), 0.0);
    vec3 diffuse =  diff * texture(u_DiffuseMap, oi_TexCoordinate).rgb;

    //specular
    vec3 viewDir = normalize(u_ViewPos - oi_Position);
    vec3 reflectDir = reflect(-lightDirection, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),2.0);
    vec3 specular = spec * texture(u_DiffuseMap, oi_TexCoordinate).rgb;

    // attenuation
    vec3 distance   = u_LightPos - oi_Position;
    vec3 direction  = projection(distance, oi_Normal);
    vec3 light_radius = distance - direction;
    float radius_length = length(light_radius);
    // standard deviation
    float s = 2.25;
    // expectation
    float e = 0.0;
    float attenuation = (SDP / s) * exp(-0.5*pow(((radius_length - e)/s),2.0));
    //ambient  * attenuation;
    //diffuse  * attenuation;
    //specular * attenuation;

    //vec3 result = u_LightColor * (ambient + diffuse + specular) + u_Emission;
    vec4 result = texture(u_DiffuseMap, oi_TexCoordinate)*oi_Color+vec4(diffuse,1.0);
    o_FragColor = result;
}