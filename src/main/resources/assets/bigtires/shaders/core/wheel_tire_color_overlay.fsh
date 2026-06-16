#version 150

uniform sampler2D Sampler0;   /* color_mask.png: R=шина, G=диск */

in vec4 vertexColor;          /* dye RGB × lightmap */
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    /* R-канал маски = интенсивность окраски шины (резина).
       0.0 = tint не применяется, 1.0 = полный tint цвет. */
    float mask = texture(Sampler0, texCoord0).r;

    if (mask < 0.004) discard;

    fragColor = vec4(vertexColor.rgb, mask * vertexColor.a);
}