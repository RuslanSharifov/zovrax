#version 120
/* DRAWBUFFERS:0 */
uniform sampler2D texture;
uniform sampler2D lightmap;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 vertexColor;
void main() {
    vec4 albedo = texture2D(texture, texcoord) * vertexColor;
    if (albedo.a < 0.01) discard;
    // Respect vanilla lightmap to keep held items and particles visible.
    vec3 illumination = max(vec3(0.35), texture2D(lightmap, lmcoord).rgb);
    gl_FragData[0] = vec4(albedo.rgb * illumination, albedo.a);
}
