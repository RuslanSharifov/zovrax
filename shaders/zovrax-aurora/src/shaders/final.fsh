#version 120
#include "/lib/settings.glsl"
#include "/lib/sky.glsl"
uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D depthtex0;
uniform sampler2D depthtex1;
uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelView;
uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform vec3 moonPosition;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform float viewWidth;
uniform float viewHeight;
uniform int worldTime;
varying vec2 texcoord;

// Iris normally clears colortex1 to white. It must be black so that only
// explicitly tagged water surfaces receive screen-space geometry reflections.
const vec4 colortex1ClearColor=vec4(0.0,0.0,0.0,0.0);

vec3 zxEye(vec2 uv,float depth) {
    vec4 homogeneous=gbufferProjectionInverse*vec4(uv*2.0-1.0,depth*2.0-1.0,1.0);
    return homogeneous.xyz/homogeneous.w;
}
vec3 zxTone(vec3 c) {
#ifdef TONEMAPPING
    c=max(c,vec3(0.0))*EXPOSURE;
    c=clamp((c*(2.51*c+0.03))/(c*(2.43*c+0.59)+0.14),0.0,1.0);
#endif
    vec3 grading=vec3(c.r*1.035+c.g*0.010,
                      c.g*1.065+c.b*0.012,
                      c.b*1.09+c.r*0.004);
    c=mix(c,grading,COLOR_STRENGTH);
    float luma=dot(c,vec3(0.2126,0.7152,0.0722));
    c=mix(vec3(luma),c,SATURATION);
    c=(c-0.5)*CONTRAST+0.5;
    return clamp(c,0.0,1.0);
}
float zxAO(vec2 uv,float depth) {
#ifdef SSAO
    vec2 stepSize=vec2(1.9/viewWidth,1.9/viewHeight);
    float occlusion=0.0;
    for(int i=0;i<4;i++){
        vec2 pos=(i==0?vec2(1.0,0.0):i==1?vec2(-1.0,0.0):i==2?vec2(0.0,1.0):vec2(0.0,-1.0));
        float sampleDepth=texture2D(depthtex0,clamp(uv+pos*stepSize,0.001,0.999)).r;
        occlusion+=step(depth+0.001,sampleDepth);
    }
    return 1.0-occlusion*0.25*SSAO_STRENGTH;
#else
    return 1.0;
#endif
}
vec3 zxScreenReflection(vec2 uv,vec3 eye,vec3 normalWorld,out float confidence) {
    confidence=0.0;
    vec3 normalEye=normalize(mat3(gbufferModelView)*normalWorld);
    vec3 reflected=normalize(reflect(normalize(eye),normalEye));
    vec3 start=eye+normalEye*0.12;
    vec3 sampled=vec3(0.0);
#ifdef SCREEN_REFLECTIONS
    for(int i=1;i<=SSR_STEPS;i++) {
        float traveled=float(i)*1.35;
        vec3 point=start+reflected*traveled;
        vec4 clip=gbufferProjection*vec4(point,1.0);
        if(clip.w<=0.01)break;
        vec2 screen=clip.xy/clip.w*0.5+0.5;
        if(any(lessThanEqual(screen,vec2(0.02))) || any(greaterThanEqual(screen,vec2(0.98))))break;
        float sceneDepth=texture2D(depthtex1,screen).r;
        if(sceneDepth>=0.9999)continue;
        vec3 sceneEye=zxEye(screen,sceneDepth);
        // Intersection only when the ray is close to the opaque surface.
        float error=abs(point.z-sceneEye.z);
        if(error < (0.35+0.12*traveled)) {
            sampled=texture2D(colortex0,screen).rgb;
            confidence=clamp(1.0-traveled/float(SSR_STEPS)*0.13,0.0,1.0);
            confidence*=smoothstep(0.01,0.07,screen.x)*smoothstep(0.01,0.07,screen.y);
            break;
        }
    }
#endif
    return sampled;
}
void main() {
    vec2 uv=texcoord;
    float depth=texture2D(depthtex0,uv).r;
    vec3 color=texture2D(colortex0,uv).rgb;
    vec3 sunWorld=normalize(mat3(gbufferModelViewInverse)*sunPosition);
    vec3 moonWorld=normalize(mat3(gbufferModelViewInverse)*moonPosition);
    float day=zxDay(sunWorld);
    vec3 eye=zxEye(uv,min(depth,0.99999));
    vec3 worldRay=normalize(mat3(gbufferModelViewInverse)*eye);
    if(depth>=0.9999) {
        color=zxSky(worldRay,sunWorld,moonWorld,cameraPosition,frameTimeCounter,rainStrength);
    } else {
        color*=zxAO(uv,depth);
#ifdef SCREEN_REFLECTIONS
        vec4 material=texture2D(colortex1,uv);
        if(material.a>0.5) {
            float hit=0.0;
            vec3 reflection=zxScreenReflection(uv,eye,normalize(material.rgb*2.0-1.0),hit);
            color=mix(color,reflection,hit*SSR_STRENGTH);
        }
#endif
#ifdef DISTANCE_FOG
        float distance=length(eye);
        float atmosphere=(1.0-exp(-distance/max(FOG_DISTANCE,1.0)));
        atmosphere*=FOG_STRENGTH*smoothstep(16.0,60.0,distance);
        vec3 fog=zxSky(normalize(vec3(worldRay.x,max(worldRay.y,0.07),worldRay.z)),
                       sunWorld,moonWorld,cameraPosition,frameTimeCounter,rainStrength);
        color=mix(color,fog,clamp(atmosphere,0.0,0.9));
#endif
#ifdef SUN_SHAFTS
        vec4 clip=gbufferProjection*vec4(sunPosition,1.0);
        if(clip.w>0.0 && day>0.05) {
            vec2 sunUV=clip.xy/clip.w*0.5+0.5;
            vec2 ray=sunUV-uv;
            float radial=clamp(1.0-length(ray)*1.6,0.0,1.0);
            if(radial>0.001 && all(greaterThan(sunUV,vec2(0.0))) && all(lessThan(sunUV,vec2(1.0)))){
                float light=0.0;
                for(int i=1;i<=SUN_SHAFT_SAMPLES;i++){
                    vec2 p=uv+ray*(float(i)/float(SUN_SHAFT_SAMPLES));
                    light+=step(0.9999,texture2D(depthtex0,p).r);
                }
                color+=vec3(1.0,0.78,0.52)*(light/float(SUN_SHAFT_SAMPLES))*
                      radial*radial*SUN_SHAFT_STRENGTH*day*(1.0-rainStrength);
            }
        }
#endif
    }
#ifdef BLOOM
    vec3 glow=vec3(0.0);
    vec2 pixel=vec2(3.0/viewWidth,3.0/viewHeight);
    for(int i=0;i<4;i++){
        vec2 off=(i==0?vec2(-1.0,0.0):i==1?vec2(1.0,0.0):i==2?vec2(0.0,-1.0):vec2(0.0,1.0));
        glow+=max(texture2D(colortex0,clamp(uv+off*pixel,0.0,1.0)).rgb-vec3(0.75),vec3(0.0));
    }
    color+=glow*BLOOM_STRENGTH*0.25;
#endif
#ifdef VIGNETTE
    vec2 center=uv*2.0-1.0;
    color*=1.0-dot(center,center)*VIGNETTE_STRENGTH;
#endif
    gl_FragColor=vec4(zxTone(color),1.0);
}
