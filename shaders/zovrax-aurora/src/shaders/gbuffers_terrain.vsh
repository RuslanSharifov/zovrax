#version 120
#include "/lib/settings.glsl"
#include "/lib/wind.glsl"
attribute vec2 mc_Entity;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 vertexColor;
varying vec3 eyePos;
varying vec3 eyeNormal;
varying float blockKind;
void main() {
    blockKind = mc_Entity.x;
    vec3 moved = zxWind(gl_Vertex.xyz, cameraPosition, frameTimeCounter, blockKind);
    vec4 view = gl_ModelViewMatrix * vec4(moved, 1.0);
    eyePos = view.xyz;
    eyeNormal = normalize(gl_NormalMatrix * gl_Normal);
    gl_Position = gl_ProjectionMatrix * view;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    vertexColor = gl_Color;
}
