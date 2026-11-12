#ifndef ZOVRAX_SKY
#define ZOVRAX_SKY
#include "/lib/noise.glsl"
#define ZX_PI 3.14159265359

float zxDay(vec3 sunDir) {
    return smoothstep(-0.10, 0.12, sunDir.y);
}
float zxStars(vec3 direction) {
#ifdef NIGHT_STARS
    vec2 sphere = vec2(atan(direction.z, direction.x), asin(clamp(direction.y, -1.0, 1.0)));
    vec2 cell = sphere * vec2(430.0, 310.0);
    vec2 id = floor(cell);
    vec2 center = vec2(zxHash(id + 11.9), zxHash(id + 67.3));
    float distanceToStar = length(fract(cell) - center);
    float mask = step(1.0 - 0.005 * STAR_DENSITY, zxHash(id + 2.3));
    float twinkle = 0.85 + 0.15 * sin(id.x * 3.3 + id.y * 1.7);
    return mask * (1.0 - smoothstep(0.012, 0.075, distanceToStar)) * STAR_BRIGHTNESS * twinkle;
#else
    return 0.0;
#endif
}
float zxCloudAmount(vec3 direction, vec3 cameraPos, float time) {
#ifdef PROCEDURAL_CLOUDS
    if (direction.y <= 0.01) return 0.0;
    float dist = max(20.0, (CLOUD_HEIGHT - cameraPos.y) / max(direction.y, 0.04));
    // The far horizon fades out before texture scale and rays explode.
    vec2 at = cameraPos.xz + direction.xz * min(dist, 2100.0);
    float density = zxCloudField(at, time);
    float shape = smoothstep(1.0 - CLOUD_COVERAGE - 0.12, 1.0 - CLOUD_COVERAGE + 0.11, density);
    return shape * CLOUD_DENSITY * smoothstep(0.02, 0.16, direction.y);
#else
    return 0.0;
#endif
}
vec3 zxSky(vec3 d, vec3 sun, vec3 moon, vec3 cameraPos, float time, float rain) {
    d = normalize(d);
    sun = normalize(sun);
    moon = normalize(moon);
    float day = zxDay(sun);
    float horizon = 1.0 - pow(max(d.y, 0.0), 0.48);
    float dusk = (1.0 - day) * smoothstep(-0.22, 0.03, sun.y);
    vec3 zenith = mix(vec3(0.006, 0.011, 0.034), vec3(0.15, 0.37, 0.72), day);
    vec3 nearHorizon = mix(vec3(0.022, 0.045, 0.091), vec3(0.56, 0.73, 0.92), day);
    nearHorizon = mix(nearHorizon, vec3(0.95, 0.43, 0.25), dusk * 0.55);
    vec3 col = mix(zenith, nearHorizon, clamp(horizon, 0.0, 1.0));
    float sunAligned = max(dot(d, sun), 0.0);
    float moonAligned = max(dot(d, moon), 0.0);
#ifdef ATMOSPHERIC_HAZE
    col += mix(vec3(0.08, 0.10, 0.17), vec3(0.85, 0.53, 0.22), day) *
           pow(sunAligned, 24.0) * 0.19 * SUN_GLOW;
#endif
    col += vec3(1.0, 0.84, 0.61) * smoothstep(0.99945, 0.99988, sunAligned) * SUN_GLOW * day * 1.5;
    col += vec3(0.64, 0.78, 1.0) * smoothstep(0.99967, 0.99993, moonAligned) * MOON_GLOW * (1.0 - day);
    col += vec3(0.44, 0.67, 1.0) * pow(moonAligned, 150.0) * MOON_GLOW * (1.0 - day) * 0.22;
    col += vec3(0.82, 0.90, 1.0) * zxStars(d) * (1.0 - day) * (1.0 - rain);
#ifdef AURORA_BOREALIS
    // Small, cheap northern lights with no expensive raymarching.
    float ribbon = sin(d.x * 24.0 + time * 0.03 + 2.2 * sin(d.z * 7.0));
    float band = exp(-pow((d.y - 0.28 - 0.13 * ribbon) * 7.0, 2.0));
    col += vec3(0.06, 0.25, 0.19) * band * (1.0 - day) * (1.0 - rain) * 0.55;
#endif
#ifdef PROCEDURAL_CLOUDS
    float coverage = zxCloudAmount(d, cameraPos, time);
    float highlight = 0.56 + 0.44 * max(dot(d, sun), 0.0);
    vec3 cloudColor = mix(vec3(0.085, 0.11, 0.18), vec3(0.92, 0.93, 0.97), day);
    cloudColor *= highlight + 0.26;
    col = mix(col, cloudColor, coverage * (1.0 - 0.42 * rain));
#endif
    // Darker weather with a muted bluish tint.
    col = mix(col, col * vec3(0.53, 0.59, 0.66), rain * 0.45);
    return col;
}
#endif
