#ifndef ZOVRAX_WIND
#define ZOVRAX_WIND
vec3 zxWind(vec3 localPosition, vec3 cameraPos, float time, float blockId) {
    vec3 result = localPosition;
    vec3 world = localPosition + cameraPos;
    float gust = sin(time * 0.72 * WIND_SPEED + world.x * 0.19 + world.z * 0.11);
    gust += 0.34 * sin(time * 1.65 * WIND_SPEED + world.x * 0.51 - world.z * 0.36);
    // Flex leaves naturally; anchored plant bases move less.
    if (blockId > 100.5 && blockId < 101.5) {
#ifdef WAVING_LEAVES
        result.x += 0.055 * WIND_STRENGTH * gust;
        result.z += 0.048 * WIND_STRENGTH * sin(time * WIND_SPEED + world.z * 0.28);
#endif
    } else if (blockId > 101.5 && blockId < 103.5) {
#ifdef WAVING_PLANTS
        float rootWeight = clamp(fract(world.y) * 1.5, 0.05, 1.0);
        result.x += 0.10 * rootWeight * WIND_STRENGTH * gust;
        result.z += 0.07 * rootWeight * WIND_STRENGTH * gust;
#endif
    }
    return result;
}
#endif
