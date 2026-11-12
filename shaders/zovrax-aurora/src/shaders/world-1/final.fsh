#version 120
uniform sampler2D colortex0;
varying vec2 texcoord;
// Preserve Nether atmosphere; the Overworld procedural sky is disabled here.
void main() {
    gl_FragColor = vec4(texture2D(colortex0, texcoord).rgb, 1.0);
}
