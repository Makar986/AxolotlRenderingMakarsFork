#version 120

uniform sampler2D Sampler0;

varying vec4 vertexColor;
varying vec2 texCoord0;

void main() {
    vec4 color = vec4(vertexColor.rgb, vertexColor.a * texture2D(Sampler0, texCoord0).r);
    if (color.a == 0.0) {
        discard;
    }
    gl_FragColor = color;
}
