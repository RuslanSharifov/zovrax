#version 120
/* DRAWBUFFERS:0 */
#include "/lib/settings.glsl"
#include "/lib/sky.glsl"
uniform sampler2D texture;
uniform sampler2D lightmap;
uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform vec3 moonPosition;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
uniform float rainStrength;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 vertexColor;
varying vec3 eyeNormal;
varying vec3 eyePos;
varying float blockKind;
void main() {
    vec4 albedo = texture2D(texture, texcoord) * vertexColor;
    if (albedo.a < 0.03) discard;
    if (blockKind < 103.5 || blockKind > 104.5) {
        gl_FragData[0] = albedo;
        return;
    }
    vec3 viewDir = normalize(-eyePos);
    vec3 normal = normalize(eyeNormal);
    vec3 worldNormal = normalize(mat3(gbufferModelViewInverse) * normal);
    vec3 sunW = normalize(mat3(gbufferModelViewInverse) * sunPosition);
    vec3 moonW = normalize(mat3(gbufferModelViewInverse) * moonPosition);
    float day = zxDay(sunW);
#ifdef WATER_WAVES
    vec3 world = (gbufferModelViewInverse * vec4(eyePos, 1.0)).xyz + cameraPosition;
    float phase = world.x * 0.61 + world.z * 0.77 + frameTimeCounter * 1.3;
    worldNormal = normalize(worldNormal + vec3(0.12 * cos(phase), 0.0, 0.11 * sin(phase * 0.85)));
#endif
    vec3 viewDirectionWorld = normalize(mat3(gbufferModelViewInverse) * viewDir);
    float fresnel = pow(1.0 - clamp(dot(worldNormal, viewDirectionWorld), 0.0, 1.0), 4.0);
    vec3 baseWater = mix(vec3(0.03, 0.18, 0.26), albedo.rgb, 0.42);
    vec3 mixedColor = baseWater;
#ifdef WATER_REFLECTIONS
    vec3 reflected = reflect(-viewDirectionWorld, worldNormal);
    vec3 skyReflection = zxSky(reflected, sunW, moonW, cameraPosition, frameTimeCounter, rainStrength);
    mixedColor = mix(baseWater, skyReflection, (0.20 + 0.80 * fresnel) * WATER_REFLECTIVITY);
#endif
#ifdef SUN_REFLECTIONS
    vec3 lightView = normalize(mix(moonPosition, sunPosition, day));
    float spark = pow(max(dot(reflect(-lightView, normal), viewDir), 0.0), 96.0);
    mixedColor += mix(vec3(0.40, 0.52, 0.80) * NIGHT_LIGHT,
                      vec3(1.0, 0.83, 0.53), day) * spark * 0.85;
#endif
    mixedColor = mix(mixedColor * 0.62, mixedColor, clamp(texture2D(lightmap, lmcoord).r, 0.0, 1.0));
    gl_FragData[0] = vec4(mixedColor, WATER_OPACITY);
}
