#version 120

uniform vec4 Dimensions;
uniform vec4 Rounding;

varying vec2 texCoord0;
varying vec4 vertexColor;
varying vec4 dimensions;
varying vec4 rounding;

void main() {
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    texCoord0 = vec2(gl_MultiTexCoord0.xy);
	vertexColor = gl_Color;
	rounding = Rounding;
	dimensions = vec4(gl_Vertex.xy - Dimensions.xy, Dimensions.zw);
}
