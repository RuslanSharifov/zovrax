#ifndef ZOVRAX_AURORA_SETTINGS
#define ZOVRAX_AURORA_SETTINGS
// Zovrax Aurora 0.2.0-beta: stronger visible lighting and atmosphere.
// Each toggle below is a real shader pass branch, not a decorative menu item.
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
#define SCREEN_REFLECTIONS
#define PROCEDURAL_CLOUDS
#define NIGHT_STARS
#define SUN_SHAFTS
#define ATMOSPHERIC_HAZE
#define DISTANCE_FOG
#define TONEMAPPING
#define VIGNETTE
// #define CLOUD_SHADOWS
// #define SSAO
// #define BLOOM
// #define AURORA_BOREALIS

const int shadowMapResolution = 1024; // [512 1024 1536 2048]
const float shadowDistance = 64.0; // [32.0 48.0 64.0 96.0 128.0]
#define SHADOW_SAMPLES 2 // [1 2 4]
#define SHADOW_SOFTNESS 1.0 // [0.5 1.0 1.5 2.0]
#define SHADOW_CONTRAST 0.68 // [0.25 0.45 0.68 0.85]
#define LEAF_LIGHT_STRENGTH 0.72 // [0.0 0.25 0.55 0.72 1.0]
#define LEAF_DITHER_AMOUNT 0.36 // [0.0 0.15 0.30 0.36 0.45]
#define NIGHT_LIGHT 0.34 // [0.10 0.18 0.26 0.34 0.50]
#define SUNLIGHT_STRENGTH 1.10 // [0.5 0.7 0.9 1.10 1.35]
#define SUN_WARMTH 0.85 // [0.0 0.4 0.65 0.85 1.0]
#define TORCH_WARMTH 0.75 // [0.0 0.35 0.75 1.0 1.3]
#define SUN_SHAFT_STRENGTH 0.40 // [0.0 0.15 0.28 0.40 0.65]
#define SUN_SHAFT_SAMPLES 4 // [2 4 6 8]
#define FOG_DISTANCE 120.0 // [60.0 80.0 120.0 160.0 240.0]
#define FOG_STRENGTH 0.60 // [0.0 0.3 0.6 0.85 1.0]

#define WIND_STRENGTH 0.78 // [0.0 0.35 0.65 0.78 1.0 1.5]
#define WIND_SPEED 1.0 // [0.25 0.5 1.0 1.5 2.0]
#define WATER_WAVE_HEIGHT 0.095 // [0.0 0.04 0.075 0.095 0.12 0.18]
#define WATER_OPACITY 0.64 // [0.35 0.45 0.60 0.64 0.75 0.9]
#define WATER_REFLECTIVITY 0.82 // [0.0 0.25 0.55 0.75 0.82 1.0]
#define WATER_BRIGHTNESS 1.15 // [0.6 0.85 1.0 1.15 1.4]
#define SSR_STEPS 8 // [4 8 12 16]
#define SSR_STRENGTH 0.48 // [0.0 0.25 0.48 0.7 1.0]

#define CLOUD_DETAIL 3 // [2 3 4]
#define CLOUD_COVERAGE 0.60 // [0.2 0.35 0.52 0.60 0.65 0.80]
#define CLOUD_DENSITY 0.90 // [0.25 0.5 0.78 0.90 1.0]
#define CLOUD_SPEED 0.42 // [0.0 0.15 0.30 0.42 0.6 1.0]
#define CLOUD_SCALE 1.0 // [0.5 0.75 1.0 1.5 2.0]
#define CLOUD_HEIGHT 165.0 // [120.0 165.0 220.0 280.0]
#define STAR_DENSITY 0.85 // [0.15 0.35 0.65 0.85 1.0]
#define STAR_BRIGHTNESS 1.25 // [0.35 0.65 0.95 1.25 1.6]
#define SUN_GLOW 1.20 // [0.4 0.7 1.0 1.2 1.3 1.7]
#define MOON_GLOW 1.10 // [0.3 0.5 0.8 1.1 1.5]

#define EXPOSURE 1.16 // [0.7 0.85 1.0 1.16 1.35]
#define SATURATION 1.16 // [0.7 0.85 1.0 1.06 1.16 1.2 1.4]
#define CONTRAST 1.12 // [0.85 1.0 1.04 1.12 1.15 1.3]
#define COLOR_STRENGTH 0.80 // [0.0 0.3 0.5 0.8 1.0]
#define BLOOM_STRENGTH 0.22 // [0.0 0.1 0.22 0.4 0.65]
#define SSAO_STRENGTH 0.33 // [0.15 0.33 0.55 0.8]
#define VIGNETTE_STRENGTH 0.10 // [0.0 0.08 0.1 0.12 0.2 0.35]
#endif
