#version 120
#include "/lib/settings.glsl"
#include "/lib/noise.glsl"
uniform sampler2D texture;
varying vec2 texcoord;
varying float blockKind;
void main() {
    vec4 albedo = texture2D(texture, texcoord);
    if (albedo.a < 0.10) discard;
#ifdef LEAF_SHADOW_DITHER
    if (blockKind > 100.5 && blockKind < 101.5) {
        vec2 position = floor(gl_FragCoord.xy);
        // Dappled sunlight passes between leaf clusters in the shadow map.
        if (zxHash(position) < LEAF_DITHER_AMOUNT) discard;
    }
#endif
    gl_FragColor = vec4(1.0);
}
