#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 3) in vec4 Rounding;
layout(location = 4) in vec4 Dimensions;
layout(location = 5) in vec4 Effect0;
layout(location = 6) in vec4 Effect1;
layout(location = 7) in vec4 Effect2;
layout(location = 8) in vec4 Effect3;

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec4 rounding;
layout(location = 2) out vec4 dimensions;
layout(location = 3) out vec4 effect0;
layout(location = 4) out vec4 effect1;
layout(location = 5) out vec4 effect2;
layout(location = 6) out vec4 effect3;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
	vertexColor = Color;
	rounding = Rounding;
	dimensions = Dimensions;
	effect0 = Effect0;
	effect1 = Effect1;
	effect2 = Effect2;
	effect3 = Effect3;
}
