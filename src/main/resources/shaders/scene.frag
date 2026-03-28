#version 330

in vec2 outTexCoords;
out vec4 fragColor;
uniform sampler2D txtSampler;

uniform int isWireframe;
uniform vec4 wireframeColor;

void main()
{
    if (isWireframe == 1) {
        fragColor = wireframeColor;
    } else {
        fragColor = texture(txtSampler, outTexCoords);
    }
}
