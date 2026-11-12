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
float zxShadow(vec3 eye, vec3 n, vec3 lightDir) {
#ifndef SHADOWS
    return 1.0;
#else
#ifndef NIGHT_SHADOWS
    if (worldTime > 12500 && worldTime < 23000) return 1.0;
#endif
    vec4 relative = gbufferModelViewInverse * vec4(eye,1.0);
    vec4 projected = shadowProjection * shadowModelView * relative;
    vec3 uv = projected.xyz / max(projected.w,0.0001) * 0.5 + 0.5;
    if (any(lessThanEqual(uv,vec3(0.0))) || any(greaterThanEqual(uv,vec3(1.0))))
        return 1.0;
    float bias = max(0.0006, 0.0019*(1.0-max(dot(n,lightDir),0.0)));
    float sum=0.0;
    for(int i=0;i<SHADOW_SAMPLES;i++) {
        vec2 pixel=vec2(mod(float(i),2.0),floor(float(i)*0.5)) - 0.5;
#ifdef SOFT_SHADOWS
        pixel *= SHADOW_SOFTNESS/float(shadowMapResolution);
#else
        pixel = vec2(0.0);
#endif
        sum += step(uv.z-bias,texture2D(shadowtex0,uv.xy+pixel).r);
    }
    return sum/float(SHADOW_SAMPLES);
#endif
}
void main() {
    vec4 albedo=texture2D(texture,texcoord)*vertexColor;
    if(albedo.a<0.11)discard;
    vec3 n=normalize(eyeNormal);
    vec3 sun=normalize(sunPosition);
    vec3 lightDirection=normalize(shadowLightPosition);
    float day=smoothstep(-0.12,0.12,sin(float(worldTime)*0.0002617993878));
    float lambert=max(dot(n,lightDirection),0.0);
    float shadow=zxShadow(eyePos,n,lightDirection);
    vec3 vanilla=texture2D(lightmap,lmcoord).rgb;
    float skylight=clamp(lmcoord.y,0.0,1.0);
    float blocklight=clamp(lmcoord.x,0.0,1.0);
    float diffuse=lambert*(1.0-SHADOW_CONTRAST*(1.0-shadow));
    vec3 ambient=mix(vec3(0.08,0.12,0.23)*NIGHT_LIGHT,
                     vec3(0.24,0.32,0.45),day);
    ambient*=0.35+0.65*skylight;
    vec3 directColor=mix(vec3(0.27,0.43,0.70)*NIGHT_LIGHT,
                         mix(vec3(0.92,0.96,1.0),vec3(1.0,0.80,0.56),SUN_WARMTH),day);
    vec3 direct=directColor*diffuse*SUNLIGHT_STRENGTH*skylight;
    vec3 torch=vec3(1.0,0.55,0.24)*pow(blocklight,2.2)*TORCH_WARMTH*0.48;
    vec3 lighting=ambient+direct+torch+vanilla*0.48;
#ifdef LEAF_TRANSMISSION
    if(blockKind>100.5 && blockKind<101.5) {
        float backlit=max(dot(-n,sun),0.0);
        lighting+=vec3(0.23,0.54,0.11)*backlit*LEAF_LIGHT_STRENGTH*day*skylight;
    }
#endif
#ifdef CLOUD_SHADOWS
    vec3 world=(gbufferModelViewInverse*vec4(eyePos,1.0)).xyz+cameraPosition;
    float cloud=zxCloudField(world.xz,frameTimeCounter);
    lighting*=1.0-0.25*smoothstep(0.43,0.7,cloud)*day;
#endif
    lighting=mix(lighting,lighting*vec3(0.72,0.80,0.91),rainStrength*0.35);
    gl_FragData[0]=vec4(albedo.rgb*lighting,albedo.a);
}
