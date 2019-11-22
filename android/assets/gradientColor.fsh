#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord0;

uniform vec2 u_resolution;
uniform vec2 u_centerPosition;
uniform vec4 u_color;
uniform sampler2D u_sampler2D;

void main(){
    vec4 color = texture2D(u_sampler2D, v_texCoord0) * v_color;
    color.rgb = u_color.rgb;
    gl_FragColor =  color;
}
