attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_Color;
varying vec2 v_TexCoord;

void main() {
    v_Color = a_color;
    v_TexCoord = a_texCoord0;
    gl_Position =  u_projTrans * a_position;
}