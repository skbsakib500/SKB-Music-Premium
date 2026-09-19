// Copyright (c) 2016-2021 Maksim M. Petrov (aka Max MP)
precision mediump float;

attribute vec4 a_position;
attribute vec4 a_texCoord;
 
uniform float u_pixelSize; // Original pixel size
 
varying vec2 blurCoordinates[5];
 
void main()
{
	gl_Position = a_position;

	blurCoordinates[0] = a_texCoord.xy; // Should sample 4 pixels in case of 2x downsample
	blurCoordinates[1] = a_texCoord.xy + vec2(-u_pixelSize, -u_pixelSize);
	blurCoordinates[2] = a_texCoord.xy + vec2(u_pixelSize, -u_pixelSize);
	blurCoordinates[3] = a_texCoord.xy + vec2(-u_pixelSize, u_pixelSize);
	blurCoordinates[4] = a_texCoord.xy + vec2(u_pixelSize, u_pixelSize);
}
