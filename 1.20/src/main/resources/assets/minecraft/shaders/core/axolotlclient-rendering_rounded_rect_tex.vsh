#version 330

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec4 Rounding;
in vec4 Dimensions;

out vec2 texCoord0;
out vec4 vertexColor;
out vec4 dimensions;
out vec4 rounding;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
	vertexColor = Color;
	rounding = Rounding;
	dimensions = Dimensions;
}
