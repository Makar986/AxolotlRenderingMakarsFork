#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec4 Rounding;
layout(location = 3) in vec4 Dimensions;
layout(location = 4) in float OutlineWidth;

layout(location = 3) out float outlineWidth;
layout(location = 2) out vec4 vertexColor;
layout(location = 1) out vec4 dimensions;
layout(location = 0) out vec4 rounding;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

	vertexColor = Color;
	rounding = Rounding;
	dimensions = Dimensions;
    outlineWidth = OutlineWidth;
}
