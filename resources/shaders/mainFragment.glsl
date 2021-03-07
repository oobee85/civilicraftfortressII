#version 330 core


in vec2 passTextureCoord;
in vec3 passColor;

out vec4 outColor;

uniform sampler2D textureSampler;

void main() {
//	outColor = texture(textureSampler, passTextureCoord);
	outColor = texture(textureSampler, passTextureCoord) * vec4(passColor, 1.0);
//	outColor = vec4(passColor, 1.0);
}
