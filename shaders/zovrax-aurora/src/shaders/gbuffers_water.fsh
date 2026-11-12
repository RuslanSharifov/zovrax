#version 120
/* DRAWBUFFERS:01 */
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
    vec4 albedo=texture2D(texture,texcoord)*vertexColor;
    if(albedo.a<0.03)discard;
    // Water is a distinct gbuffer material: secondary attachment carries
    // water normals for the optional screen-space geometry reflection.
    if(blockKind<103.5 || blockKind>104.5) {
        gl_FragData[0]=albedo;
        gl_FragData[1]=vec4(0.0);
        return;
    }
    vec3 viewDirection=normalize(-eyePos);
    vec3 nEye=normalize(eyeNormal);
    vec3 nWorld=normalize(mat3(gbufferModelViewInverse)*nEye);
    vec3 sunWorld=normalize(mat3(gbufferModelViewInverse)*sunPosition);
    vec3 moonWorld=normalize(mat3(gbufferModelViewInverse)*moonPosition);
    float day=zxDay(sunWorld);
#ifdef WATER_WAVES
    vec3 world=(gbufferModelViewInverse*vec4(eyePos,1.0)).xyz+cameraPosition;
    float phase=world.x*0.75+world.z*0.57+frameTimeCounter*1.4;
    nWorld=normalize(nWorld+vec3(0.19*cos(phase),0.0,0.16*sin(phase*1.11)));
#endif
    vec3 viewWorld=normalize(mat3(gbufferModelViewInverse)*viewDirection);
    float fresnel=pow(1.0-clamp(dot(nWorld,viewWorld),0.0,1.0),4.2);
    vec3 water=vec3(0.018,0.15,0.27)+albedo.rgb*0.30;
    vec3 color=water;
#ifdef WATER_REFLECTIONS
    vec3 reflected=reflect(-viewWorld,nWorld);
    vec3 sky=zxSky(reflected,sunWorld,moonWorld,cameraPosition,frameTimeCounter,rainStrength);
    color=mix(water,sky,(0.24+0.76*fresnel)*WATER_REFLECTIVITY);
#endif
#ifdef SUN_REFLECTIONS
    vec3 lightView=normalize(mix(moonPosition,sunPosition,day));
    float sparkle=pow(max(dot(reflect(-lightView,nEye),viewDirection),0.0),85.0);
    color+=mix(vec3(0.29,0.46,0.85)*NIGHT_LIGHT,vec3(1.0,0.86,0.61),day)*sparkle*1.24;
#endif
    color*=WATER_BRIGHTNESS;
    color=mix(color*0.68,color,clamp(texture2D(lightmap,lmcoord).r+0.15,0.0,1.0));
    gl_FragData[0]=vec4(color,WATER_OPACITY);
    // Packed world-space normal is only sampled by final.glsl for water pixels.
    gl_FragData[1]=vec4(nWorld*0.5+0.5,1.0);
}
