#version 330 core


in vec2 passTextureCoord;
in vec3 passColor;
in float passUseTexture;

out vec4 outColor;

uniform sampler2D textureSampler;

void main() {
//	outColor = texture(textureSampler, passTextureCoord);
	vec4 textureColor = texture(textureSampler, passTextureCoord);
	outColor = (passUseTexture*textureColor + (1-passUseTexture)*vec4(1, 1, 1, 1)) * vec4(passColor, 1.0);
//	outColor = vec4(passColor, 1.0);
}
