#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

// Copy of the frame taken right before the GUI pass (see BackdropTexture).
uniform sampler2D Sampler0;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec4 rounding;
layout(location = 2) in vec4 dimensions;
layout(location = 3) in vec4 effect0; // blurRadius, sigma

layout(location = 0) out vec4 fragColor;

float sdRoundBox(in vec2 pos, in vec2 halfDim, in vec4 radius) {
	radius.xy = (pos.x > 0.0) ? radius.xy : radius.zw;
	radius.x  = (pos.y > 0.0) ? radius.x  : radius.y;
	vec2 q = abs(pos) - halfDim + radius.x;
	return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

void main() {
	vec2 hDim = dimensions.zw / 2.0;
	float d = sdRoundBox(dimensions.xy - hDim, hDim, rounding);
	float alphaMask = 1.0 - smoothstep(0.0, 0.5, d);
	if (alphaMask <= 0.0) {
		discard;
	}

	vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
	vec2 uv = gl_FragCoord.xy * texel;

	float r = clamp(effect0.x > 0.0 ? effect0.x : 6.0, 0.0, 16.0);
	float sig = effect0.y > 0.0 ? effect0.y : 3.5;
	vec4 blurred = vec4(0.0);
	float totalWeight = 0.0;
	for (float x = -r; x <= r; x += 2.0) {
		for (float y = -r; y <= r; y += 2.0) {
			float dist = length(vec2(x, y));
			if (dist <= r) {
				float w = exp(-(dist * dist) / (2.0 * sig * sig));
				blurred += texture(Sampler0, uv + vec2(x, y) * texel) * w;
				totalWeight += w;
			}
		}
	}

	vec4 color = totalWeight > 0.0 ? blurred / totalWeight : texture(Sampler0, uv);
	vec4 tint = vertexColor * ColorModulator;
	if (tint.a > 0.0) {
		color.rgb = mix(color.rgb, tint.rgb, tint.a);
	}
	fragColor = vec4(color.rgb, alphaMask);
}
