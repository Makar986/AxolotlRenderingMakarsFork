#version 330

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec4 Rounding;
in vec4 Dimensions;
in float OutlineWidth;

out float outlineWidth;
out vec2 texCoord0;
out vec4 vertexColor;
out vec4 rounding;
out vec4 dimensions;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
	vertexColor = Color;
	rounding = Rounding;
	dimensions = Dimensions;
    outlineWidth = OutlineWidth;
}
