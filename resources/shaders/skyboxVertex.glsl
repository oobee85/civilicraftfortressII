#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 objectColor;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec2 textureCoord;

uniform mat4 projection;
uniform mat4 view;

uniform float daylight;

out vec3 passTextureCoord;
out float brightness;

void main() {
	gl_Position = projection * view * vec4(position, 1.0);
	passTextureCoord = position;
	brightness = daylight;
}
