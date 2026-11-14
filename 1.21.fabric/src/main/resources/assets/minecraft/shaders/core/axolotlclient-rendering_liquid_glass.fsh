#version 330

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

uniform vec4 Optics0; // refThickness, refFactor, refDisp, refFresRange
uniform vec4 Optics1; // refFresHard, refFresFac, glareRange, glareHard
uniform vec4 Border;  // borderWidth, borderColor.rgb
uniform vec4 Extra;   // blurRadius, shadowExpand, shadowFactor, unused

in vec2 texCoord0;
in vec4 vertexColor;
in vec4 dimensions;
in vec4 rounding;

out vec4 fragColor;

vec3 sdgBox(in vec2 p, in vec2 b, vec4 ra) {
	ra.xy = (p.x > 0.0) ? ra.xy : ra.zw;
	float r = (p.y > 0.0) ? ra.x : ra.y;
	vec2 w = abs(p) - (b - r);
	vec2 s = vec2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);
	float g = max(w.x, w.y);
	vec2 q = max(w, 0.0);
	float l = length(q);
	float dist = (g > 0.0) ? l - r : g - r;
	vec2 n = (g > 0.0) ? (q / max(l, 1e-6)) : ((w.x > w.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0));
	return vec3(dist, s * n);
}

void main() {
	vec2 halfDim = 0.5 * dimensions.zw;
	vec2 localPos = dimensions.xy - halfDim;
	vec3 g = sdgBox(localPos, halfDim, rounding);
	float dist = g.x;
	vec2 normal = g.yz;
	float nlen = length(normal);
	if (nlen > 1e-6) normal /= nlen;

	float aa = smoothstep(0.5, -0.5, dist);
	if (aa <= 0.001) {
		discard;
	}

	float refThickness = Optics0.x > 0.0 ? Optics0.x : 13.15;
	float refFactor    = Optics0.y > 0.0 ? Optics0.y : 1.4;
	float refDisp      = Optics0.z > 0.0 ? Optics0.z : 7.0;
	float refFresRange = Optics0.w > 0.0 ? Optics0.w : 39.71;

	float refFresHard  = Optics1.x > 0.0 ? Optics1.x : 0.2;
	float refFresFac   = Optics1.y > 0.0 ? Optics1.y : 0.2;
	float glareRange   = Optics1.z > 0.0 ? Optics1.z : 30.0;
	float glareHard    = Optics1.w > 0.0 ? Optics1.w : 0.2;

	float blurRadius   = Extra.x > 0.0 ? Extra.x : 8.0;
	float borderWidth  = Border.x;
	vec4  borderColor  = vec4(Border.yzw, 1.0);

	ivec2 texSize = textureSize(Sampler0, 0);
	vec2 texel = 1.0 / vec2(texSize);
	vec2 uv = gl_FragCoord.xy * texel;

	// Snell's Law physical refraction (ReGlass)
	float nmerged = -dist;
	float xR = 1.0 - nmerged / max(refThickness, 1e-4);
	float thetaI = asin(pow(clamp(xR, 0.0, 1.0), 2.0));
	float thetaT = asin((1.0 / max(refFactor, 1e-4)) * sin(thetaI));
	float edgeFactor = -tan(thetaT - thetaI);
	if (nmerged >= refThickness) edgeFactor = 0.0;

	vec2 refrOffset = -normal * edgeFactor * 0.08;

	// Chromatic dispersion offsets (ReGlass coefficients)
	const float NR = 0.985;
	const float NG = 1.000;
	const float NB = 1.015;
	vec2 offR = refrOffset * (1.0 - (NR - 1.0) * refDisp) * 40.0 * texel;
	vec2 offG = refrOffset * (1.0 - (NG - 1.0) * refDisp) * 40.0 * texel;
	vec2 offB = refrOffset * (1.0 - (NB - 1.0) * refDisp) * 40.0 * texel;

	// Multi-tap frosted blur with chromatic dispersion
	vec4 dispPixel = vec4(0.0);
	float rad = clamp(blurRadius, 0.0, 12.0);
	if (rad > 0.5) {
		float stepVal = max(rad / 3.0, 1.0);
		float tw = 0.0;
		for (float dx = -rad; dx <= rad; dx += stepVal) {
			for (float dy = -rad; dy <= rad; dy += stepVal) {
				float distSq = dx * dx + dy * dy;
				if (distSq <= rad * rad) {
					float w = exp(-distSq / (rad * rad * 0.5 + 1.0));
					vec2 tap = vec2(dx, dy) * texel;
					dispPixel.r += texture(Sampler0, uv + offR + tap).r * w;
					dispPixel.g += texture(Sampler0, uv + offG + tap).g * w;
					dispPixel.b += texture(Sampler0, uv + offB + tap).b * w;
					tw += w;
				}
			}
		}
		dispPixel /= tw;
		dispPixel.a = 1.0;
	} else {
		dispPixel.r = texture(Sampler0, uv + offR).r;
		dispPixel.g = texture(Sampler0, uv + offG).g;
		dispPixel.b = texture(Sampler0, uv + offB).b;
		dispPixel.a = 1.0;
	}

	// Tint mixing
	vec4 tint = vertexColor * ColorModulator;
	vec4 outColor = mix(dispPixel, vec4(tint.rgb, 1.0), tint.a * 0.8);

	// ReGlass Fresnel edge reflection
	float fresnelFactor = clamp(pow(1.0 + dist / 1500.0 * pow(500.0 / max(refFresRange, 1e-4), 2.0) + refFresHard, 5.0), 0.0, 1.0);
	vec3 fresTint = mix(vec3(1.0), tint.rgb, tint.a * 0.5);
	outColor = mix(outColor, vec4(fresTint, 1.0), fresnelFactor * refFresFac * 0.7 * nlen);

	// ReGlass Glare
	float glareGeo = clamp(pow(1.0 + dist / 1500.0 * pow(500.0 / max(glareRange, 1e-4), 2.0) + glareHard, 5.0), 0.0, 1.0);
	vec3 glareMix = mix(dispPixel.rgb, tint.rgb, tint.a * 0.5);
	outColor.rgb = mix(outColor.rgb, glareMix, 0.25 * glareGeo * nlen);

	// ReGlass Specular Rim Light
	vec2 lightDirection = normalize(vec2(-1.0, 1.0));
	float specular = pow(max(dot(normal, lightDirection), 0.0), 2.0);
	float rim = exp(-max(-dist, 0.0) / 2.04);
	vec3 lightTint = mix(vec3(1.0), tint.rgb, tint.a * 0.15);
	outColor.rgb = mix(outColor.rgb, lightTint, clamp(0.12 * rim * specular, 0.0, 0.16));

	// Optional crisp border
	if (borderWidth > 0.0) {
		float borderDist = abs(dist + borderWidth * 0.5) - borderWidth * 0.5;
		float borderMask = smoothstep(0.5, -0.5, borderDist);
		outColor = mix(outColor, borderColor, borderMask * borderColor.a);
	}

	fragColor = vec4(outColor.rgb, outColor.a * aa);
}
