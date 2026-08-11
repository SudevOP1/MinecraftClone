#version 330

in vec2 outTexCoords;
in float outLight;
out vec4 fragColor;
uniform sampler2D txtSampler;

uniform int isWireframe;
uniform vec4 wireframeColor;
uniform int alphaCutout;

void main()
{
    if (isWireframe == 1) {
        fragColor = wireframeColor;
    } else {
        vec4 color = texture(txtSampler, outTexCoords);
        // Cutout materials (leaves) draw in the opaque pass; drop the see-through
        // texels instead of blending them so draw order stops mattering.
        if (alphaCutout == 1 && color.a < 0.5) {
            discard;
        }
        fragColor = vec4(color.rgb * outLight, color.a);
    }
}
