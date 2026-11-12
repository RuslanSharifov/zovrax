#version 120
/* DRAWBUFFERS:0 */
#include "/lib/settings.glsl"
#include "/lib/noise.glsl"
uniform sampler2D texture;
uniform sampler2D lightmap;
uniform sampler2D shadowtex0;
uniform mat4 gbufferModelViewInverse;
uniform mat4 shadowModelView;
uniform mat4 shadowProjection;
uniform vec3 shadowLightPosition;
uniform vec3 sunPosition;
uniform vec3 cameraPosition;
uniform int worldTime;
uniform float frameTimeCounter;
uniform float rainStrength;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 vertexColor;
varying vec3 eyePos;
varying vec3 eyeNormal;
varying float blockKind;
float zxShadow(vec3 viewPosition, vec3 normalView, vec3 lightDirection) {
#ifndef SHADOWS
    return 1.0;
#else
#ifndef NIGHT_SHADOWS
    if (worldTime > 12500 && worldTime < 23000) return 1.0;
#endif
    vec4 relWorld = gbufferModelViewInverse * vec4(viewPosition, 1.0);
    vec4 shadow = shadowProjection * shadowModelView * relWorld;
    vec3 uv = shadow.xyz / shadow.w * 0.5 + 0.5;
    if (uv.x <= 0.0 || uv.x >= 1.0 || uv.y <= 0.0 || uv.y >= 1.0 ||
        uv.z <= 0.0 || uv.z >= 1.0) return 1.0;
    float bias = max(0.00065, 0.0018 * (1.0 - max(dot(normalView, lightDirection), 0.0)));
    float visibility = 0.0;
    const int taps = SHADOW_SAMPLES;
    for (int i = 0; i < taps; ++i) {
        vec2 offset = vec2(mod(float(i), 2.0), floor(float(i) * 0.5)) - vec2(0.5);
        // Four taps are useful on high quality; one/two taps on low.
#ifdef SOFT_SHADOWS
        offset *= SHADOW_SOFTNESS / float(shadowMapResolution);
#else
        offset = vec2(0.0);
#endif
        float occluder = texture2D(shadowtex0, uv.xy + offset).r;
        visibility += (uv.z - bias < occluder) ? 1.0 : 0.0;
    }
    return visibility / float(taps);
#endif
}
void main() {
    vec4 texel = texture2D(texture, texcoord) * vertexColor;
    if (texel.a < 0.11) discard;
    vec3 n = normalize(eyeNormal);
    vec3 sunDirection = normalize(sunPosition);
    vec3 dir = normalize(shadowLightPosition);
    float day = smoothstep(-0.08, 0.10, sin(float(worldTime) * 0.0002617993878));
    float ndotl = max(dot(n, dir), 0.0);
    vec3 lm = texture2D(lightmap, lmcoord).rgb;
    vec3 lit = max(vec3(0.09), lm * 0.83);
    float shade = zxShadow(eyePos, n, dir);
    vec3 warm = mix(vec3(0.36, 0.45, 0.78) * NIGHT_LIGHT,
                    vec3(1.00, 0.94, 0.79) * SUNLIGHT_STRENGTH, day);
    lit += warm * ndotl * shade * (0.55 + 0.45 * lmcoord.y);
#ifdef LEAF_TRANSMISSION
    if (blockKind > 100.5 && blockKind < 101.5) {
        float transmit = max(dot(-n, sunDirection), 0.0);
        lit += vec3(0.25, 0.56, 0.13) * transmit * LEAF_LIGHT_STRENGTH * day;
    }
#endif
#ifdef CLOUD_SHADOWS
    vec3 w = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz + cameraPosition;
    float cloudShade = zxCloudField(w.xz, frameTimeCounter);
    lit *= 1.0 - 0.28 * smoothstep(0.35, 0.75, cloudShade) * day;
#endif
    lit *= 1.0 - 0.14 * rainStrength;
    gl_FragData[0] = vec4(texel.rgb * lit, texel.a);
}
