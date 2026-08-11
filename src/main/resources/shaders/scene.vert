#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoords;
layout (location=2) in float light;

out vec2 outTexCoords;
out float outLight;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main()
{
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    outTexCoords = texCoords;
    outLight = light;
}