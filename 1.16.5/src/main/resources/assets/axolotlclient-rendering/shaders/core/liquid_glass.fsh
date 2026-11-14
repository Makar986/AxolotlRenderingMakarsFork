#version 120

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float BlurRadius;
uniform float Distortion;
uniform vec4 GlassTint;
uniform vec4 BorderColor;
uniform float BorderWidth;

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
	vec2 pos = dimensions.xy - hDim;
	float d = sdRoundBox(pos, hDim, rounding);
	float alphaMask = 1.0 - smoothstep(0.0, 0.7, d);
	if (alphaMask <= 0.0) {
		discard;
	}

	vec2 uv = gl_FragCoord.xy / ScreenSize;
	vec2 texel = 1.0 / ScreenSize;

	// Surface normal approximation for optical refraction towards curved borders
	vec2 norm = pos / max(hDim, vec2(1.0));
	float edgeFactor = clamp(-d / max(min(rounding.x, 14.0), 3.0), 0.0, 1.0);
	float distFactor = (1.0 - edgeFactor) * (Distortion > 0.0 ? Distortion : 5.0);

	vec2 refractedUV = uv + norm * distFactor * texel;

	// Chromatic dispersion (RGB split)
	float r = texture2D(DiffuseSampler, refractedUV + norm * 2.5 * texel).r;
	float g = texture2D(DiffuseSampler, refractedUV).g;
	float b = texture2D(DiffuseSampler, refractedUV - norm * 2.5 * texel).b;
	vec3 baseColor = vec3(r, g, b);

	// Frosted blur sample integration
	float blurR = max(BlurRadius, 1.0);
	if (blurR > 1.5) {
		vec3 blurAcc = vec3(0.0);
		float totalW = 0.0;
		for (float i = -blurR; i <= blurR; i += 3.0) {
			for (float j = -blurR; j <= blurR; j += 3.0) {
				float dist = length(vec2(i, j));
				if (dist <= blurR) {
					float w = exp(-(dist * dist) / (2.0 * blurR * blurR));
					blurAcc += texture2D(DiffuseSampler, refractedUV + vec2(i, j) * texel).rgb * w;
					totalW += w;
				}
			}
		}
		if (totalW > 0.0) {
			baseColor = mix(baseColor, blurAcc / totalW, 0.65);
		}
	}

	// Tint mixing
	vec4 tint = GlassTint.a > 0.0 ? GlassTint : vertexColor;
	vec3 glass = mix(baseColor, tint.rgb, tint.a);

	// Specular highlight: simulate top-left directional light
	vec2 lightDir = normalize(vec2(-0.4, -0.9));
	float specular = max(0.0, dot(norm, lightDir));
	specular = pow(specular, 3.0) * (1.0 - edgeFactor) * 0.45;

	// Soft interior rim reflection
	float rim = pow(1.0 - edgeFactor, 1.6) * 0.28;

	vec3 finalRgb = glass + vec3(specular + rim);

	// Outer/Inner border
	if (BorderWidth > 0.0) {
		float borderAlpha = smoothstep(-BorderWidth - 0.5, -BorderWidth + 0.5, d);
		finalRgb = mix(finalRgb, BorderColor.rgb, borderAlpha * BorderColor.a);
	}

	gl_FragColor = vec4(finalRgb, alphaMask);
}
