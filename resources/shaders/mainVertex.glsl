#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 tempcolor;

uniform vec3 off;
uniform mat4 projection;

out vec3 color;

void main() {
	vec3 temp = position + off;
	temp.y = temp.y * temp.x;
	gl_Position = projection * vec4(temp, 1.0);
//	gl_Position = vec4(temp, 1.0);
	color = vec3(gl_Position.x, gl_Position.y - gl_Position.x, gl_Position.y);
//	color = tempcolor;
}
