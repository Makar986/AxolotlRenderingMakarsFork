#version 120

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

varying vec4 vertexColor;
varying vec2 texCoord0;

void main() {
    gl_Position =  gl_ModelViewProjectionMatrix * gl_Vertex;

    vertexColor = gl_Color;
    texCoord0 = vec2(gl_MultiTexCoord0.xy);
}
