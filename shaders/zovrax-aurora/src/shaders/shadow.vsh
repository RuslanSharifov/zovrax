#version 120
#include "/lib/settings.glsl"
#include "/lib/wind.glsl"
attribute vec2 mc_Entity;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
uniform mat4 shadowModelViewInverse;
varying vec2 texcoord;
varying float blockKind;
void main() {
    blockKind = mc_Entity.x;
    vec3 world = (shadowModelViewInverse * gl_ModelViewMatrix * gl_Vertex).xyz + cameraPosition;
    vec3 moved = zxWind(gl_Vertex.xyz, world, frameTimeCounter, blockKind);
    gl_Position = gl_ModelViewProjectionMatrix * vec4(moved, 1.0);
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
