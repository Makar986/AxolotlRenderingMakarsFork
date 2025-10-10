#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vec4(vertexColor.rgb, vertexColor.a * texture(Sampler0, texCoord0).r);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
