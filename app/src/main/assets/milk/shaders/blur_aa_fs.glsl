// Copyright (c) 2016-2022 Maksim M. Petrov (aka Max MP)
precision mediump float;

uniform sampler2D sampler_blur;
uniform float u_mult;
uniform float u_saturation;
uniform bool u_apply_light;
uniform float u_pin_light;
uniform float u_lighten;
uniform float u_darken;

varying vec2 blurCoordinates[5];
 
void main() {
	lowp vec4 sum = vec4(0.0);
	sum += texture2D(sampler_blur, blurCoordinates[1]);
	sum += texture2D(sampler_blur, blurCoordinates[2]);
	sum += texture2D(sampler_blur, blurCoordinates[3]);
	sum += texture2D(sampler_blur, blurCoordinates[4]);

	sum.rgb *= u_mult;

	const lowp vec3 W = vec3(0.2125, 0.7154, 0.0721);
	lowp vec3 intensity = vec3(dot(sum.rgb, W));
	sum.rgb = mix(intensity, sum.rgb, u_saturation);

	if(u_apply_light) {
		vec3 src = sum.rgb;
		if(u_pin_light >= 0.0) {
			vec3 dst = vec3(u_pin_light);
			src = mix(mix(2.0 * src, dst, step(0.5 * dst, src)), max(vec3(0.0), 2.0 * src - 1.0), step(dst, (2.0 * src - 1.0)));
		}
		src = clamp(src, vec3(u_lighten), vec3(u_darken));
		sum.rgb = src;
	}

	sum.a = 1.0;
	gl_FragColor = sum; 
}
