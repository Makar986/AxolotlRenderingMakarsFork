#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec4 Rounding;
in vec4 Dimensions;
in vec4 Effect0;

out vec4 vertexColor;
out vec4 rounding;
out vec4 dimensions;
out vec4 effect0;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
	vertexColor = Color;
	rounding = Rounding;
	dimensions = Dimensions;
	effect0 = Effect0;
}
