#version 130

uniform mat4 Dimensions;
uniform vec4 Rounding;
uniform float OutlineWidth;

out float outlineWidth;
out vec4 vertexColor;
out vec4 dimensions;
out vec4 rounding;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    vertexColor = gl_Color;
    rounding = Rounding;
    dimensions = Dimensions[gl_VertexID];
    outlineWidth = OutlineWidth;
}
