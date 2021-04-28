#version 330 core

in vec3 passTextureCoord;
in float brightness;

out vec4 outColor;

uniform samplerCube textureSampler;

void main() {
	vec4 textureColor = texture(textureSampler, passTextureCoord);
//	outColor = vec4(0, 0, 0, 1);
	outColor = textureColor * brightness;
}
