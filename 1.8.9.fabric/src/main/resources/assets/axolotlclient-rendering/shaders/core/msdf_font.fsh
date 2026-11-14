#version 120

#ifdef GL_OES_standard_derivatives
#extension GL_OES_standard_derivatives : enable
#endif

uniform sampler2D Sampler0;

varying vec4 vertexColor;
varying vec2 texCoord0;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msd = texture2D(Sampler0, texCoord0).rgb;
    float sd = median(msd.r, msd.g, msd.b);
    float screenPxDistance = (sd - 0.5);
    float w = clamp(fwidth(sd), 0.0001, 1.0);
    float opacity = clamp(screenPxDistance / w + 0.5, 0.0, 1.0);

    vec4 color = vertexColor;
    color.a *= opacity;
    if (color.a <= 0.001) {
        discard;
    }
    gl_FragColor = color;
}
