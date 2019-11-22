
varying vec4 v_color;
varying vec2 v_texCoord0;

uniform vec2 u_resolution;
uniform vec2 u_position;
uniform sampler2D u_sampler2D;

//float outerRadius = .5, innerRadius = .45, intensity = 1;

void main() {

	vec4 base = texture2D(u_sampler2D, v_texCoord0) * v_color;
	vec2 relativePosition = gl_FragCoord.xy / u_resolution - .5;

	relativePosition = relativePosition - u_position/u_resolution;
//	relativePosition.y *= u_resolution.y / u_resolution.x;
	float len = length(relativePosition);

	float vignette = smoothstep(0.5, 0.45, len);

	base.a = mix(base.a, base.a * vignette, 1.0);
	gl_FragColor = base;
}
