#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 1) in vec4 vertexColor;
layout(location = 0) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

float median(float r, float g, float b) {
	return max(min(r, g), min(max(r, g), b));
}

void main() {
	vec3 msd = texture(Sampler0, texCoord0).rgb;
	float sd = median(msd.r, msd.g, msd.b);
	float w = clamp(fwidth(sd), 0.0001, 1.0);
	float opacity = clamp((sd - 0.5) / w + 0.5, 0.0, 1.0);

	vec4 color = vertexColor * ColorModulator;
	color.a *= opacity;
	if (color.a <= 0.001) {
		discard;
	}
	fragColor = color;
}
