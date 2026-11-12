#ifndef ZOVRAX_SKY
#define ZOVRAX_SKY
#include "/lib/noise.glsl"
#define ZX_PI 3.14159265359

float zxDay(vec3 sunDir) {
    return smoothstep(-0.11, 0.11, sunDir.y);
}
float zxStars(vec3 d, float time) {
#ifdef NIGHT_STARS
    // More visible star count than 0.1.x; no random temporal teleporting.
    vec2 spherical = vec2(atan(d.z, d.x) * 0.15915494 + 0.5,
                          asin(clamp(d.y, -1.0, 1.0)) * 0.31830989 + 0.5);
    vec2 grid = spherical * vec2(420.0, 240.0);
    vec2 id = floor(grid);
    vec2 center = vec2(zxHash(id + 11.9), zxHash(id + 67.3));
    float starSeed = zxHash(id + 2.3);
    float chance = 0.055 * STAR_DENSITY;
    float star = step(1.0 - chance, starSeed);
    float dist = length(fract(grid) - center);
    float radius = mix(0.055, 0.16, zxHash(id + 7.1));
    float glow = (1.0 - smoothstep(radius * 0.35, radius, dist));
    float twinkle = 0.82 + 0.18 * sin(time * 0.42 + id.x * 4.7 + id.y * 2.8);
    return star * glow * STAR_BRIGHTNESS * twinkle;
#else
    return 0.0;
#endif
}
float zxCloudAmount(vec3 direction, vec3 cameraPos, float time) {
#ifdef PROCEDURAL_CLOUDS
    if(direction.y <= 0.015) return 0.0;
    float distanceToLayer = (CLOUD_HEIGHT - cameraPos.y) / max(direction.y, 0.035);
    vec2 pos = cameraPos.xz + direction.xz * clamp(distanceToLayer, 24.0, 1800.0);
    float coarse = zxCloudField(pos, time);
    float strands = zxNoise(pos * 0.013 / CLOUD_SCALE + vec2(time * CLOUD_SPEED * 0.006, 5.3));
    float density = mix(coarse, strands, 0.22);
    float threshold = 0.66 - CLOUD_COVERAGE * 0.44;
    float shape = smoothstep(threshold - 0.10, threshold + 0.10, density);
    return shape * CLOUD_DENSITY * smoothstep(0.025, 0.13, direction.y);
#else
    return 0.0;
#endif
}
vec3 zxSky(vec3 direction, vec3 sun, vec3 moon, vec3 cameraPos, float time, float rain) {
    vec3 d = normalize(direction), s = normalize(sun), m = normalize(moon);
    float day = zxDay(s);
    float horizon = pow(clamp(1.0 - max(d.y, 0.0), 0.0, 1.0), 0.55);
    float sunrise = smoothstep(-0.22, 0.05, s.y) * (1.0 - smoothstep(0.05, 0.34, s.y));
    float sunsetLobe = pow(max(dot(normalize(vec3(d.x, max(d.y, 0.0), d.z)),
                                    normalize(vec3(s.x, max(s.y, 0.02), s.z))), 0.0), 3.5);
    vec3 nightTop = vec3(0.008, 0.015, 0.048);
    vec3 nightHorizon = vec3(0.028, 0.060, 0.11);
    vec3 dayTop = vec3(0.095, 0.30, 0.70);
    vec3 dayHorizon = vec3(0.52, 0.74, 0.95);
    vec3 sky = mix(mix(nightTop, nightHorizon, horizon),
                   mix(dayTop, dayHorizon, horizon), day);
    vec3 dusk = vec3(1.0, 0.36, 0.105) * sunrise * sunsetLobe * (0.18 + 0.82 * horizon);
    sky += dusk;
    float sunDot = max(dot(d,s),0.0);
    float moonDot = max(dot(d,m),0.0);
    sky += vec3(1.0, 0.66, 0.30) * pow(sunDot, 110.0) * SUN_GLOW * day * 0.42;
    sky += vec3(1.0, 0.93, 0.73) * smoothstep(0.99945, 0.99987, sunDot) * SUN_GLOW * day * 1.8;
    sky += vec3(0.35, 0.50, 0.82) * pow(moonDot, 85.0) * MOON_GLOW * (1.0-day) * 0.30;
    sky += vec3(0.75, 0.85, 1.0) * smoothstep(0.9995, 0.99993, moonDot) * MOON_GLOW * (1.0-day);
    sky += vec3(0.72, 0.83, 1.0) * zxStars(d,time) * (1.0-day) * (1.0-rain) * smoothstep(-0.02,0.13,d.y);
#ifdef AURORA_BOREALIS
    float ribbon = sin(d.x * 27.0 + time * 0.025 + 1.6*sin(d.z*8.0));
    float band = exp(-pow((d.y - 0.25 - 0.10*ribbon)*7.0,2.0));
    sky += vec3(0.025,0.22,0.14) * band * (1.0-day) * (1.0-rain)*0.75;
#endif
#ifdef PROCEDURAL_CLOUDS
    float density = zxCloudAmount(d,cameraPos,time);
    float forward = max(dot(d,s),0.0);
    float silver = pow(forward,13.0)*0.75 + 0.18;
    vec3 clouds = mix(vec3(0.10,0.14,0.24),vec3(0.94,0.95,0.97),day);
    clouds += vec3(0.22,0.16,0.10)*silver*day;
    clouds *= 0.78 + silver*0.4;
    sky = mix(sky,clouds,density);
#endif
    sky = mix(sky,sky*vec3(0.51,0.60,0.68),rain*0.56);
    return sky;
}
#endif
