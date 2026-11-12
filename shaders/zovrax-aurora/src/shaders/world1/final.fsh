#version 120
uniform sampler2D colortex0;
varying vec2 texcoord;
// Preserve The End dimension sky instead of inserting Overworld clouds.
void main() {
    gl_FragColor = vec4(texture2D(colortex0, texcoord).rgb, 1.0);
}
