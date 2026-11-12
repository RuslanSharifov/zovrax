#ifndef ZOVRAX_NOISE
#define ZOVRAX_NOISE
float zxHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}
float zxNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(zxHash(i), zxHash(i + vec2(1.0, 0.0)), f.x),
               mix(zxHash(i + vec2(0.0, 1.0)), zxHash(i + vec2(1.0, 1.0)), f.x), f.y);
}
// Stationary noise uses world position; wind changes only the offset.
float zxCloudField(vec2 worldXZ, float time) {
    vec2 p = worldXZ * (0.0035 / CLOUD_SCALE) + vec2(time * CLOUD_SPEED * 0.007, time * CLOUD_SPEED * 0.003);
    float weight = 0.55;
    float sum = 0.0;
    float total = 0.0;
    for (int octave = 0; octave < CLOUD_DETAIL; ++octave) {
        sum += zxNoise(p) * weight;
        total += weight;
        p = p * 2.15 + vec2(14.1, 3.8);
        weight *= 0.52;
    }
    return sum / max(total, 0.001);
}
#endif
