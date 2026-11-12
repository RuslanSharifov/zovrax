#ifndef ZOVRAX_AURORA_SETTINGS
#define ZOVRAX_AURORA_SETTINGS

// Zovrax Aurora 0.1.0: toggles are detected by Iris / shader menu.
#define SHADOWS
#define NIGHT_SHADOWS
#define SOFT_SHADOWS
#define LEAF_TRANSMISSION
#define LEAF_SHADOW_DITHER
#define WAVING_LEAVES
#define WAVING_PLANTS
#define WATER_WAVES
#define WATER_REFLECTIONS
#define SUN_REFLECTIONS
#define PROCEDURAL_CLOUDS
#define NIGHT_STARS
#define SUN_SHAFTS
#define ATMOSPHERIC_HAZE
#define TONEMAPPING
#define VIGNETTE
// #define CLOUD_SHADOWS
// #define SSAO
// #define BLOOM
// #define AURORA_BOREALIS

// Distance and shadow-map size dominate shadow cost.
const int shadowMapResolution = 1024; // [512 1024 1536 2048]
const float shadowDistance = 64.0; // [32.0 48.0 64.0 96.0 128.0]

#define SHADOW_SAMPLES 2 // [1 2 4]
#define SHADOW_SOFTNESS 1.0 // [0.5 1.0 1.5 2.0]
#define LEAF_LIGHT_STRENGTH 0.55 // [0.0 0.25 0.55 0.8 1.0]
#define LEAF_DITHER_AMOUNT 0.30 // [0.0 0.15 0.30 0.45]
#define NIGHT_LIGHT 0.26 // [0.10 0.18 0.26 0.36 0.50]
#define SUNLIGHT_STRENGTH 0.70 // [0.3 0.5 0.7 0.9 1.2]
#define SUN_SHAFT_STRENGTH 0.28 // [0.0 0.15 0.28 0.45 0.7]
#define SUN_SHAFT_SAMPLES 4 // [2 4 6 8]

#define WIND_STRENGTH 0.65 // [0.0 0.35 0.65 1.0 1.5]
#define WIND_SPEED 1.0 // [0.25 0.5 1.0 1.5 2.0]
#define WATER_WAVE_HEIGHT 0.075 // [0.0 0.04 0.075 0.12 0.18]
#define WATER_OPACITY 0.60 // [0.35 0.45 0.60 0.75 0.9]
#define WATER_REFLECTIVITY 0.55 // [0.0 0.25 0.55 0.75 1.0]

#define CLOUD_DETAIL 2 // [2 3 4]
#define CLOUD_COVERAGE 0.52 // [0.2 0.35 0.52 0.65 0.80]
#define CLOUD_DENSITY 0.78 // [0.25 0.5 0.78 1.0]
#define CLOUD_SPEED 0.30 // [0.0 0.15 0.30 0.6 1.0]
#define CLOUD_SCALE 1.0 // [0.5 0.75 1.0 1.5 2.0]
#define CLOUD_HEIGHT 165.0 // [120.0 165.0 220.0 280.0]
#define STAR_DENSITY 0.65 // [0.15 0.35 0.65 0.85 1.0]
#define STAR_BRIGHTNESS 0.95 // [0.35 0.65 0.95 1.25 1.6]
#define SUN_GLOW 1.0 // [0.4 0.7 1.0 1.3 1.7]
#define MOON_GLOW 0.8 // [0.3 0.5 0.8 1.1 1.5]

#define EXPOSURE 1.0 // [0.7 0.85 1.0 1.15 1.35]
#define SATURATION 1.06 // [0.7 0.85 1.0 1.06 1.2 1.4]
#define CONTRAST 1.04 // [0.85 1.0 1.04 1.15 1.3]
#define BLOOM_STRENGTH 0.22 // [0.0 0.1 0.22 0.4 0.65]
#define SSAO_STRENGTH 0.33 // [0.15 0.33 0.55 0.8]
#define VIGNETTE_STRENGTH 0.12 // [0.0 0.08 0.12 0.2 0.35]
#endif
