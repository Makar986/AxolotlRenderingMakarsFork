#version 120

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float BlurRadius;
uniform float Sigma;

varying vec4 vertexColor;
varying vec4 rounding;
varying vec4 dimensions;

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

	vec2 uv = gl_FragCoord.xy / ScreenSize;
	vec2 texel = 1.0 / ScreenSize;
	vec4 blurred = vec4(0.0);
	float totalWeight = 0.0;
	float r = max(BlurRadius, 1.0);
	float sig = max(Sigma, 1.0);

	for (float x = -r; x <= r; x += 2.0) {
		for (float y = -r; y <= r; y += 2.0) {
			float dist = length(vec2(x, y));
			if (dist <= r) {
				float w = exp(-(dist * dist) / (2.0 * sig * sig));
				blurred += texture2D(DiffuseSampler, uv + vec2(x, y) * texel) * w;
				totalWeight += w;
			}
		}
	}

	vec4 color = (totalWeight > 0.0) ? (blurred / totalWeight) : texture2D(DiffuseSampler, uv);
	if (vertexColor.a > 0.0) {
		color.rgb = mix(color.rgb, vertexColor.rgb, vertexColor.a);
	}
	color.a = alphaMask;
	gl_FragColor = color;
}
