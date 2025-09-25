#version 130

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

varying vec3 Position;
varying vec4 Color;
varying vec4 Rounding;
varying vec4 Dimensions;

varying vec4 vertexColor;
varying vec4 dimensions;
varying vec4 rounding;

void main() {
	gl_Position = gl_ModelViewMatrix*gl_ProjectionMatrix*/*ModelViewMat * ProjMat */ vec4(Position, 1.0);

	vertexColor = Color;
    rounding = Rounding;
    dimensions = Dimensions;
}
