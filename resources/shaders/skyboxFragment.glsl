#version 330 core

in vec3 passTextureCoord;
in vec3 sunColorPass;

out vec4 outColor;

uniform samplerCube textureSampler;

void main() {
	vec4 textureColor = texture(textureSampler, passTextureCoord);
	outColor = textureColor * vec4(sunColorPass, 1);

}
