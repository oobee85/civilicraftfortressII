#version 330 core


in vec2 passTextureCoord;
in vec3 passColor;
in float passUseTexture;
in vec3 passReflect;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform samplerCube cubeMap;

void main() {
//	outColor = texture(textureSampler, passTextureCoord);
	vec4 textureColor1 = texture(textureSampler, passTextureCoord);
	vec4 textureColor2 = texture(cubeMap, passReflect);

	vec4 textureColor = textureColor1*0.6 + textureColor2*0.4;
	outColor = (passUseTexture*textureColor + (1-passUseTexture)*vec4(1, 1, 1, 1)) * vec4(passColor, 1.0);
//	outColor = vec4(passColor, 1.0);
}
