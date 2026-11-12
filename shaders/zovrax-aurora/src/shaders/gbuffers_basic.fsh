#version 120
/* DRAWBUFFERS:0 */
varying vec4 vertexColor;
void main() {
    gl_FragData[0] = vertexColor;
}
