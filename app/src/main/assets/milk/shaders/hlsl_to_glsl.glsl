// Copyright (c) 2016-2021 Maksim M. Petrov (aka Max MP)
#if __VERSION__ < 300  // We still should have implicit GL_OES_texture_3D for gles 3.+
#ifdef GL_OES_texture_3D
#extension GL_OES_texture_3D : enable
#else
#define sampler3D sampler2D // GLES2 assumed
#define texture3D(sampler, uvw) texture2D(sampler, (uvw).xy)
#endif
#endif

#ifdef GL_EXT_shader_non_constant_global_initializers
#extension GL_EXT_shader_non_constant_global_initializers : enable
#endif

#define sampler sampler2D

#define xlv_TEXCOORD0 v_texCoord
// warp only
#define xlv_TEXCOORD1 v_texCoord_orig 

#if __VERSION__ > 100
layout(location = 0) out lowp vec4 _out_color;
#endif


