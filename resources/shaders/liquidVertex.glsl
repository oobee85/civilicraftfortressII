#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 objectColor;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec2 textureCoord;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

uniform vec3 sunDirection;
uniform vec3 sunColor;
uniform vec3 ambientColor;
uniform float useTexture;
uniform float isHighlight;
uniform float waveOffset;
uniform float waveAmplitude;

out vec3 passColor;
out vec2 passTextureCoord;
out float passUseTexture;
out vec3 passReflect;


mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main() {

	vec4 worldPos = model * vec4(position, 1.0);

	vec4 worldNormal = normalize(model * vec4(normal, 0));
	vec4 cameraPosition = inverse(view) * vec4(0, 0, 0, 1);

	float angle1 = worldPos.y*2 - waveOffset*0.005;
	float angle2 = worldPos.x*2 - waveOffset*0.006;
	worldPos.z += 0.03*(1 + sin(angle1)) * waveAmplitude;
//	worldNormal = normalize(worldNormal + 0.1*vec4(0, 1, 0, 0) * cos(angle1));

	vec4 camToPos = cameraPosition - worldPos;
	vec4 reflect = camToPos - 2 * dot(camToPos, worldNormal) * worldNormal;
	passReflect = (rotationMatrix(vec3(1, 0, 0), -3.14159/2) * reflect).xyz;
	passReflect.x *= -1;

	vec3 lightDir = normalize(-sunDirection);
	float diffuseRatio = max(dot(worldNormal.xyz, lightDir), 0.0);
	vec3 diffuseColor = diffuseRatio * sunColor;
	vec3 highlightColor = vec3(1, 1, 0);

	gl_Position = projection * view * worldPos;
	passColor = (diffuseColor + ambientColor) * (1 - isHighlight) + highlightColor * isHighlight;
	passTextureCoord = textureCoord;
	passUseTexture = useTexture * (1 - isHighlight);
}
