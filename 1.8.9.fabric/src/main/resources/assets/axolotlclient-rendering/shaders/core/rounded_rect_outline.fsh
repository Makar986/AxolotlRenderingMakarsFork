#version 120

varying float outlineWidth;
varying vec4 rounding;
varying vec4 dimensions;
varying vec4 vertexColor;

// https://www.shadertoy.com/view/4llXD7, MIT License
float sdRoundBox(in vec2 pos, in vec2 halfDim, in vec4 radius) {
	radius.xy = (pos.x>0.0)?radius.xy : radius.zw;
	radius.x  = (pos.y>0.0)?radius.x  : radius.y;
	vec2 q = abs(pos)-halfDim+radius.x;
	return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

void main() {
	vec4 color = vertexColor;

	vec2 hDim = dimensions.zw/2.;
	float d = abs(sdRoundBox(dimensions.xy-hDim, hDim-outlineWidth, rounding-outlineWidth)) - outlineWidth;
	color.a *= 1.0-smoothstep(0.0, 0.5, d);

	if (color.a == 0.0) {
		discard;
	}
	gl_FragColor = color;
}
