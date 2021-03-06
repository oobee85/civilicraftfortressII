#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 objectColor;
layout(location = 2) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;

uniform vec3 sunDirection;
uniform vec3 sunColor;

out vec3 color;



void main() {
	gl_Position = projection * view * vec4(position, 1.0);
//	gl_Position = vec4(temp, 1.0);
//	color = vec3(gl_Position.x, gl_Position.y - gl_Position.x, gl_Position.y);

	vec3 ambientLight = vec3(0.1, 0.1, 0.1);

	vec3 norm = normalize(normal);
	vec3 lightDir = normalize(-sunDirection);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = diff * sunColor;
	color = (diffuse + ambientLight) * objectColor;
}
