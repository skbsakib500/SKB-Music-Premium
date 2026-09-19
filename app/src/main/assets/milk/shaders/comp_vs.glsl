// Copyright (c) 2016-2021 Maksim M. Petrov (aka Max MP)
precision mediump float;

attribute vec4 a_position;
attribute vec4 a_texCoord;

uniform lowp mat4 mvp_matrix;
 
varying vec4 v_texCoord;

// NOTE: this one used only for custom shaders
void main() {
	gl_Position = mvp_matrix * a_position;
	v_texCoord = a_texCoord;
}
