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
	vec2 pos = dimensions.xy - hDim;
	float d = sdRoundBox(pos, hDim, rounding);
	float alphaMask = 1.0 - smoothstep(0.0, 0.7, d);
	if (alphaMask <= 0.0) {
		discard;
	}

	ivec2 texSize = textureSize(Sampler0, 0);
	vec2 texel = 1.0 / vec2(texSize);

	// Edge normal refraction
	vec2 norm = pos / max(hDim, vec2(1.0));
	float edgeFactor = clamp(-d / max(min(rounding.x, 14.0), 3.0), 0.0, 1.0);
	float distFactor = (1.0 - edgeFactor) * 5.0;

	vec2 refractedUV = texCoord0 + norm * distFactor * texel;

	// Chromatic dispersion
	float r = texture(Sampler0, refractedUV + norm * 2.5 * texel).r;
	float g = texture(Sampler0, refractedUV).g;
	float b = texture(Sampler0, refractedUV - norm * 2.5 * texel).b;
	vec3 baseColor = vec3(r, g, b);

	// Frosted backdrop blur
	vec3 blurAcc = vec3(0.0);
	float totalW = 0.0;
	for (float i = -5.0; i <= 5.0; i += 2.5) {
		for (float j = -5.0; j <= 5.0; j += 2.5) {
			float dist = length(vec2(i, j));
			if (dist <= 5.0) {
				float w = exp(-(dist * dist) / 18.0);
				blurAcc += texture(Sampler0, refractedUV + vec2(i, j) * texel).rgb * w;
				totalW += w;
			}
		}
	}
	if (totalW > 0.0) {
		baseColor = mix(baseColor, blurAcc / totalW, 0.6);
	}

	// Tint mixing
	vec4 tint = vertexColor * ColorModulator;
	vec3 glass = mix(baseColor, tint.rgb, tint.a);

	// Specular highlight: top-left light
	vec2 lightDir = normalize(vec2(-0.4, -0.9));
	float specular = max(0.0, dot(norm, lightDir));
	specular = pow(specular, 3.0) * (1.0 - edgeFactor) * 0.45;

	// Rim reflection
	float rim = pow(1.0 - edgeFactor, 1.6) * 0.28;

	vec3 finalRgb = glass + vec3(specular + rim);

	// Crisp glass border highlight
	float borderAlpha = smoothstep(-1.5, -0.5, d);
	finalRgb = mix(finalRgb, vec3(1.0), borderAlpha * 0.35);

	fragColor = vec4(finalRgb, alphaMask);
}
