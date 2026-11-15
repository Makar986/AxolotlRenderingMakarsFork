#version 130

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vec4(vertexColor.rgb, vertexColor.a * texture2D(Sampler0, texCoord0).r);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
