#version 120
#include "/lib/settings.glsl"
#include "/lib/sky.glsl"
uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform vec3 moonPosition;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform float viewWidth;
uniform float viewHeight;
uniform int worldTime;
varying vec2 texcoord;
vec3 zxTone(vec3 v) {
#ifdef TONEMAPPING
    v *= EXPOSURE;
    v = v / (vec3(1.0) + v * 0.31);
    v = pow(max(v, vec3(0.0)), vec3(0.91));
#endif
    float grey = dot(v, vec3(0.2126, 0.7152, 0.0722));
    v = mix(vec3(grey), v, SATURATION);
    v = (v - vec3(0.5)) * CONTRAST + vec3(0.5);
    return clamp(v, 0.0, 1.0);
}
float zxAmbientOcclusion(vec2 uv, float centerDepth) {
#ifdef SSAO
    if (centerDepth >= 0.9999) return 1.0;
    vec2 stepSize = vec2(2.2 / viewWidth, 2.2 / viewHeight);
    float d1 = texture2D(depthtex0, clamp(uv + vec2(stepSize.x, 0.0), 0.001, 0.999)).r;
    float d2 = texture2D(depthtex0, clamp(uv - vec2(stepSize.x, 0.0), 0.001, 0.999)).r;
    float d3 = texture2D(depthtex0, clamp(uv + vec2(0.0, stepSize.y), 0.001, 0.999)).r;
    float d4 = texture2D(depthtex0, clamp(uv - vec2(0.0, stepSize.y), 0.001, 0.999)).r;
    float occlusion = step(centerDepth + 0.001, d1) +
                      step(centerDepth + 0.001, d2) +
                      step(centerDepth + 0.001, d3) +
                      step(centerDepth + 0.001, d4);
    return 1.0 - occlusion * 0.25 * SSAO_STRENGTH;
#else
    return 1.0;
#endif
}
void main() {
    vec2 uv = texcoord;
    float depth = texture2D(depthtex0, uv).r;
    vec3 color = texture2D(colortex0, uv).rgb;
    float day = smoothstep(-0.08, 0.10, sin(float(worldTime) * 0.0002617993878));
    if (depth >= 0.9999) {
        vec4 eye = gbufferProjectionInverse * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
        vec3 eyeDir = normalize(eye.xyz / max(eye.w, 0.0001));
        vec3 worldDir = normalize(mat3(gbufferModelViewInverse) * eyeDir);
        vec3 sunW = normalize(mat3(gbufferModelViewInverse) * sunPosition);
        vec3 moonW = normalize(mat3(gbufferModelViewInverse) * moonPosition);
        color = zxSky(worldDir, sunW, moonW, cameraPosition, frameTimeCounter, rainStrength);
    } else {
        color *= zxAmbientOcclusion(uv, depth);
#ifdef SUN_SHAFTS
        // Short screen-space shafts let light show through canopy gaps.
        vec4 clip = gbufferProjection * vec4(sunPosition, 1.0);
        if (clip.w > 0.0 && day > 0.03) {
            vec2 sunUV = clip.xy / clip.w * 0.5 + 0.5;
            vec2 ray = sunUV - uv;
            float radial = clamp(1.0 - length(ray) * 1.25, 0.0, 1.0);
            if (radial > 0.001 && all(greaterThan(sunUV, vec2(0.0))) && all(lessThan(sunUV, vec2(1.0)))) {
                float clearCount = 0.0;
                for (int i = 1; i <= SUN_SHAFT_SAMPLES; ++i) {
                    vec2 p = uv + ray * (float(i) / float(SUN_SHAFT_SAMPLES));
                    clearCount += step(0.9999, texture2D(depthtex0, p).r);
                }
                float shafts = clearCount / float(SUN_SHAFT_SAMPLES) * radial * radial;
                color += vec3(1.0, 0.82, 0.54) * shafts * SUN_SHAFT_STRENGTH * day * (1.0 - rainStrength);
            }
        }
#endif
    }
#ifdef BLOOM
    // Four small taps instead of a costly multipass HDR bloom pyramid.
    vec2 stepSize = vec2(2.5 / viewWidth, 2.5 / viewHeight);
    vec3 glow = vec3(0.0);
    for (int i = 0; i < 4; ++i) {
        vec2 offset = (i == 0 ? vec2(-1.0, 0.0) :
                       i == 1 ? vec2(1.0, 0.0) :
                       i == 2 ? vec2(0.0, -1.0) : vec2(0.0, 1.0));
        vec3 source = texture2D(colortex0, clamp(uv + offset * stepSize, 0.0, 1.0)).rgb;
        glow += max(source - vec3(0.75), vec3(0.0));
    }
    color += glow * (BLOOM_STRENGTH * 0.25);
#endif
#ifdef VIGNETTE
    vec2 centered = uv * 2.0 - 1.0;
    color *= 1.0 - dot(centered, centered) * VIGNETTE_STRENGTH;
#endif
    gl_FragColor = vec4(zxTone(color), 1.0);
}
