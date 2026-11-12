#version 120
#include "/lib/settings.glsl"
attribute vec2 mc_Entity;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 vertexColor;
varying vec3 eyeNormal;
varying vec3 eyePos;
varying float blockKind;
void main() {
    vec3 pos = gl_Vertex.xyz;
    blockKind = mc_Entity.x;
#ifdef WATER_WAVES
    if (blockKind > 103.5 && blockKind < 104.5) {
        vec3 world = pos + cameraPosition;
        float wave = sin(world.x * 0.63 + frameTimeCounter * 1.7) *
                     cos(world.z * 0.46 - frameTimeCounter * 1.4);
        pos.y += wave * WATER_WAVE_HEIGHT;
    }
#endif
    vec4 eye = gl_ModelViewMatrix * vec4(pos, 1.0);
    eyePos = eye.xyz;
    eyeNormal = normalize(gl_NormalMatrix * gl_Normal);
    gl_Position = gl_ProjectionMatrix * eye;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    vertexColor = gl_Color;
}
