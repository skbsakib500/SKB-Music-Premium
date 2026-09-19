// Copyright (c) 2016-2021 Maksim M. Petrov (aka Max MP)
precision mediump float;

uniform sampler2D sampler_blur;
uniform float u_mult;

varying vec2 blurCoordinates[5];
 
void main() {
	lowp vec4 sum = vec4(0.0);
	//sum += texture2D(sampler_blur, blurCoordinates[0]);
	sum += texture2D(sampler_blur, blurCoordinates[1]);
	sum += texture2D(sampler_blur, blurCoordinates[2]);
	sum += texture2D(sampler_blur, blurCoordinates[3]);
	sum += texture2D(sampler_blur, blurCoordinates[4]);
	sum *= u_mult; 
	gl_FragColor = vec4(sum.rgb, 1.0);
}
