#version 300 es

// Copyright (c) 2016-2021 Maksim M. Petrov (aka Max MP)
precision mediump float;

in vec4 a_position;
in vec4 a_texCoord; // warped
in vec4 a_texCoord_orig; 

uniform lowp mat4 mvp_matrix;
 
out vec4 v_texCoord;
out vec4 v_texCoord_orig;

void main() {
	gl_Position = mvp_matrix * a_position;
	v_texCoord = a_texCoord;
	v_texCoord_orig = a_texCoord_orig;
}
