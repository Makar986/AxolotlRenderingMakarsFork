#version 130

uniform mat4 Dimensions;
uniform vec4 Rounding;
uniform float OutlineWidth;

out float outlineWidth;
out vec2 texCoord0;
out vec4 vertexColor;
out vec4 rounding;
out vec4 dimensions;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    texCoord0 = vec2(gl_MultiTexCoord0.xy);
    vertexColor = gl_Color;
    rounding = Rounding;
    dimensions = Dimensions[gl_VertexID];
    outlineWidth = OutlineWidth;
}
