#version 130

uniform mat4 Dimensions;
uniform vec4 Rounding;

out vec2 texCoord0;
out vec4 vertexColor;
out vec4 dimensions;
out vec4 rounding;

void main() {
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    texCoord0 = vec2(gl_MultiTexCoord0.xy);
	vertexColor = gl_Color;
	rounding = Rounding;
	dimensions = Dimensions[gl_VertexID];
}
