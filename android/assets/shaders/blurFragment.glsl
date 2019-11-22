#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec4 v_Color;
varying vec2 v_TexCoord;

uniform sampler2D u_texture;
uniform float u_bias;

void main() {
    vec4 texColor = texture2D(u_texture, v_TexCoord, u_bias);
    gl_FragColor = texColor * v_Color;

}