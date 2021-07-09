#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 objectColor;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec2 textureCoord;

uniform mat4 projection;
uniform mat4 view;
uniform vec3 sunColor;
uniform vec3 sunPosition;

out vec3 passTextureCoord;
out vec3 sunColorPass;

void main() {
	gl_Position = projection * view * vec4(position, 1.0);
	passTextureCoord = position;

	vec3 normalizedPos = normalize(position);
	vec3 sunPos = normalize(sunPosition);
	float distance = length(normalizedPos - sunPos);
	distance = max(1 - distance, 0);
	float multiplier = distance*distance*distance;
	sunColorPass = sunColor;
	sunColorPass = sunColorPass + sunColor*multiplier;
	sunColorPass = sunColorPass * step(0, position.y);
}
