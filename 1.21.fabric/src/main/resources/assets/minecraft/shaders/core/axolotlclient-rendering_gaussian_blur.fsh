#version 330

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;
in vec4 dimensions;
in vec4 rounding;

out vec4 fragColor;

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

	ivec2 texSize = textureSize(Sampler0, 0);
	vec2 texel = 1.0 / vec2(texSize);
	vec4 blurred = vec4(0.0);
	float totalWeight = 0.0;
	float r = 6.0;
	float sig = 3.5;

	for (float x = -r; x <= r; x += 2.0) {
		for (float y = -r; y <= r; y += 2.0) {
			float dist = length(vec2(x, y));
			if (dist <= r) {
				float w = exp(-(dist * dist) / (2.0 * sig * sig));
				blurred += texture(Sampler0, texCoord0 + vec2(x, y) * texel) * w;
				totalWeight += w;
			}
		}
	}

	vec4 color = (totalWeight > 0.0) ? (blurred / totalWeight) : texture(Sampler0, texCoord0);
	vec4 tint = vertexColor * ColorModulator;
	if (tint.a > 0.0) {
		color.rgb = mix(color.rgb, tint.rgb, tint.a);
	}
	color.a = alphaMask;
	fragColor = color;
}
