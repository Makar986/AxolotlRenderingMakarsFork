#version 120

varying vec4 rounding;
varying vec4 dimensions;
varying vec4 vertexColor;

// https://www.shadertoy.com/view/3tdSDj, MIT License
float sdSegment( in vec2 p, in vec2 a, in vec2 b, float r) {
    vec2 pa = p-a, ba = b-a;
    float h = clamp( dot(pa,ba)/dot(ba,ba), 0.0, 1.0 );
    return length( pa - ba*h ) - r;
}

void main() {
	vec4 color = vertexColor;

	vec2 hDim = dimensions.zw/2.;
	float w = rounding.w;
	float factor = max(w, sqrt(w)*1.5);
    float d = sdSegment(dimensions.xy-hDim, -hDim + factor * sign(dimensions.zw), hDim - factor * sign(dimensions.zw), w);
	color.a *= 1.0-smoothstep(0.0, 0.5, d);

	if (color.a == 0.0) {
		discard;
	}
	gl_FragColor = color;
}
