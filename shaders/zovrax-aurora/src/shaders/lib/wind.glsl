#ifndef ZOVRAX_WIND
#define ZOVRAX_WIND
// Position in world coordinates is reconstructed before applying model-space
// offsets. This avoids the previous chunk-local phase mismatch.
vec3 zxWind(vec3 modelPosition, vec3 worldPosition, float time, float blockId) {
    vec3 result=modelPosition;
    float t=time*WIND_SPEED;
    float gust=sin(t*0.81+worldPosition.x*0.16+worldPosition.z*0.10);
    gust+=0.34*sin(t*1.8+worldPosition.x*0.46-worldPosition.z*0.39);
    if(blockId>100.5 && blockId<101.5) {
#ifdef WAVING_LEAVES
        result.x+=0.090*WIND_STRENGTH*gust;
        result.z+=0.075*WIND_STRENGTH*sin(t+worldPosition.z*0.23);
#endif
    } else if(blockId>101.5 && blockId<103.5) {
#ifdef WAVING_PLANTS
        float tip=clamp(fract(worldPosition.y)*1.8,0.08,1.0);
        result.x+=0.16*tip*WIND_STRENGTH*gust;
        result.z+=0.10*tip*WIND_STRENGTH*sin(t*0.95+worldPosition.x*0.33);
#endif
    }
    return result;
}
#endif
