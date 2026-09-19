#define  M_PI   3.14159265359
#define  M_PI_2 6.28318530718
#define  M_INV_PI_2  0.159154943091895
#define double float
#define double2 float2
#define double3 float3
#define double4 float4

uniform float4   rand_frame;
uniform float4   rand_preset;

uniform float4   _qa;
uniform float4   _qb;
uniform float4   _qc;
uniform float4   _qd;
uniform float4   _qe;
uniform float4   _qf;
uniform float4   _qg;
uniform float4   _qh;

#if GLES3
uniform float4x3 rot_s1;
uniform float4x3 rot_s2;
uniform float4x3 rot_s3;
uniform float4x3 rot_s4;

uniform float4x3 rot_d1;
uniform float4x3 rot_d2;  
uniform float4x3 rot_d3;
uniform float4x3 rot_d4;
uniform float4x3 rot_f1;
uniform float4x3 rot_f2;
uniform float4x3 rot_f3;
uniform float4x3 rot_f4;
uniform float4x3 rot_vf1;
uniform float4x3 rot_vf2;
uniform float4x3 rot_vf3;
uniform float4x3 rot_vf4;
uniform float4x3 rot_uf1;
uniform float4x3 rot_uf2;
uniform float4x3 rot_uf3;
uniform float4x3 rot_uf4;

uniform float4x3 rot_rand1;
uniform float4x3 rot_rand2;
uniform float4x3 rot_rand3;
uniform float4x3 rot_rand4;
#endif

#define q1 _qa.x
#define q2 _qa.y
#define q3 _qa.z
#define q4 _qa.w
#define q5 _qb.x
#define q6 _qb.y
#define q7 _qb.z
#define q8 _qb.w
#define q9 _qc.x
#define q10 _qc.y
#define q11 _qc.z
#define q12 _qc.w
#define q13 _qd.x
#define q14 _qd.y
#define q15 _qd.z
#define q16 _qd.w
#define q17 _qe.x
#define q18 _qe.y
#define q19 _qe.z
#define q20 _qe.w
#define q21 _qf.x
#define q22 _qf.y
#define q23 _qf.z
#define q24 _qf.w
#define q25 _qg.x
#define q26 _qg.y
#define q27 _qg.z
#define q28 _qg.w
#define q29 _qh.x
#define q30 _qh.y
#define q31 _qh.z
#define q32 _qh.w

#define GetMain(uv) (tex2D(sampler_main,uv).xyz)
#define GetPixel(uv) (tex2D(sampler_main,uv).xyz)
#define GetBlur1(uv) (tex2D(sampler_blur1, uv).rgb * blur1_max + blur1_min)
#define GetBlur2(uv) (tex2D(sampler_blur2, uv).rgb * blur2_max + blur2_min)
#define GetBlur3(uv) (tex2D(sampler_blur3, uv).rgb * blur3_max + blur3_min)

#define lum(x) (dot(x,float3(0.32,0.49,0.29)))
#define tex2d tex2D
#define tex3d tex3D

uniform float3 hue_shader;
uniform float _u_master_alpha;

texture   PrevFrameImage;
sampler2D sampler_main    = sampler_state { Texture = <PrevFrameImage>; };
sampler2D sampler_fc_main = sampler_state { Texture = <PrevFrameImage>; };
sampler2D sampler_pc_main = sampler_state { Texture = <PrevFrameImage>; };
sampler2D sampler_fw_main = sampler_state { Texture = <PrevFrameImage>; };
sampler2D sampler_pw_main = sampler_state { Texture = <PrevFrameImage>; };
#define sampler_FC_main sampler_fc_main
#define sampler_PC_main sampler_pc_main
#define sampler_FW_main sampler_fw_main
#define sampler_PW_main sampler_pw_main

sampler2D sampler_noise_lq;
sampler2D sampler_noise_lq_lite;
sampler2D sampler_noise_mq;
sampler2D sampler_noise_hq;
#define texsize_noise_lq      float4(256, 256, 1.0/256.0, 1.0/256.0)
#define texsize_noise_mq      float4(256, 256, 1.0/256.0, 1.0/256.0)
#define texsize_noise_hq      float4(256, 256, 1.0/256.0, 1.0/256.0)
#define texsize_noise_lq_lite float4(32, 32, 1.0/32.0, 1.0/32.0)
#define texsize_noisevol_lq   float4(32, 32, 1.0/32.0, 1.0/32.0)
#define texsize_noisevol_hq   float4(32, 32, 1.0/32.0, 1.0/32.0) 

// procedural blur textures:
sampler2D sampler_blur1;
sampler2D sampler_blur2;
sampler2D sampler_blur3;

uniform float4 roam_cos;
uniform float4 roam_sin;
uniform float4 slow_roam_cos;
uniform float4 slow_roam_sin;

uniform float blur1_min;
uniform float blur1_max;
uniform float blur2_min;
uniform float blur2_max;
uniform float blur3_min;
uniform float blur3_max;

uniform float time;
uniform float progress;
uniform float frame;
uniform float fps;
uniform float bass;
uniform float mid;
uniform float treb;
uniform float vol;
uniform float bass_att;
uniform float mid_att;
uniform float treb_att;
uniform float vol_att;
uniform float4 texsize;
uniform float4 aspect;


#define getrad     sqrt((uv.x - 0.5) * (uv.x - 0.5) * 4.0 + (uv.y - 0.5) * (uv.y - 0.5) * 4.0) * 0.7071067
#define getradWRAP distance(uv_orig, float2(.5, .5)) * 2.0
#define getang     (atan2((0.5 - uv.y), (0.5 - uv.x)) + M_PI)
#define getangWRAP (atan2((0.5 - uv.y), -(0.5 - uv.x)))
