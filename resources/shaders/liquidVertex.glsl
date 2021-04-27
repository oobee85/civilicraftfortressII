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

out vec3 passColor;
out vec2 passTextureCoord;
out float passUseTexture;



void main() {

	vec4 worldPos = model * vec4(position, 1.0);

	vec3 worldNormal = normalize(model * vec4(normal, 0)).xyz;
	vec4 cameraPosition = inverse(view) * vec4(0, 0, 0, 1);

	float angle = worldPos.y*2 - waveOffset*0.005;
	worldPos.z += 0.05*(1 + sin(angle));
	worldNormal = normalize(worldNormal + vec3(0, 1, 0) * cos(angle));



	vec3 lightDir = normalize(-sunDirection);
	float diffuseRatio = max(dot(worldNormal, lightDir), 0.0);
	vec3 diffuseColor = diffuseRatio * sunColor;
	vec3 highlightColor = vec3(1, 1, 0);

	gl_Position = projection * view * worldPos;
	passColor = (diffuseColor + ambientColor) * (1 - isHighlight) + highlightColor * isHighlight;
	passTextureCoord = textureCoord;
	passUseTexture = useTexture * (1 - isHighlight);
}


// How to do edge highlighting
//float CameraFacingPercentage = step(-1 + isHighlight*1.4, dot(fragNormal, normalize(cameraPosition.xyz-fragPosition.xyz)));
//passColor = (diffuse + ambientColor) * CameraFacingPercentage + vec3(1, 1, 0) * (1 - CameraFacingPercentage);
