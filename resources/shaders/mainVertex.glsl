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

out vec3 passColor;
out vec2 passTextureCoord;



void main() {

	vec4 fragPosition = model * vec4(position, 1.0);
	vec4 fragNormal = model * vec4(normal, 0);

//	gl_Position = vec4(temp, 1.0);
//	color = vec3(gl_Position.x, gl_Position.y - gl_Position.x, gl_Position.y);

	vec3 norm = normalize(fragNormal.xyz);
	vec3 lightDir = normalize(sunDirection);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = diff * sunColor;

	gl_Position = projection * view * fragPosition;
	passColor = (diffuse + ambientColor);
	passTextureCoord = textureCoord;
}
