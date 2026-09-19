-- Copyright (c) 2016-2025 Maksim M. Petrov (aka Max MP)
local TAG = "milk_main.lua"

local ffi = require("ffi")
local bit = require("bit")

ffi.cdef[[
int __android_log_print(int prio, const char *tag,  const char *fmt, ...);
typedef enum android_LogPriority {
    ANDROID_LOG_UNKNOWN = 0,
    ANDROID_LOG_DEFAULT,
    ANDROID_LOG_VERBOSE,
    ANDROID_LOG_DEBUG,
    ANDROID_LOG_INFO,
    ANDROID_LOG_WARN,
    ANDROID_LOG_ERROR,
    ANDROID_LOG_FATAL,
    ANDROID_LOG_SILENT,
} android_LogPriority;
]]

A = {}

function A.LOG(tag, level, message)
	ffi.C.__android_log_print(level, tag, "%s", message)
end
function A.LOGV(tag, message)
	A.LOG(tag, ffi.C.ANDROID_LOG_VERBOSE, message)
end
function A.LOGI(tag, message)
	A.LOG(tag, ffi.C.ANDROID_LOG_INFO, message)
end
function A.LOGW(tag, message)
	A.LOG(tag, ffi.C.ANDROID_LOG_WARN, message)
end
function A.LOGE(tag, message)
	A.LOG(tag, ffi.C.ANDROID_LOG_ERROR, message)
end

rawset(_G, 'print',
		function(...)
			local a = {...}
			A.LOGW("lua", table.concat(a, " "))
		end)

function ifcond(a, b, c) return a == 0 and c or b end
function below(a, b) return a < b and 1 or 0 end
function equal(a, b) if a == b then return 1 else return 0 end end
function above(a, b) return a > b and 1 or 0 end
function sign(a) return a > 0 and 1 or (a == 0 and 0 or -1) end

math.randomseed(os.time())

max = math.max
min = math.min
int = math.floor
ceil = math.ceil
abs = math.abs

sin = math.sin
cos = math.cos
acos = math.acos
asin = math.asin
sqrt = math.sqrt
tan = math.tan
atan = math.atan
atan2 = math.atan2
pow = math.pow
log = math.log
log10 = math.log10
exp = math.exp

function _bitor(a, b) return bit.bor(math.floor(a), math.floor(b)) end -- NOTE: these too differs by floor() vs int truncation, e.g will do differently for negative numbers
function _bitand(a, b) return bit.band(math.floor(a), math.floor(b)) end

function sqr(a) return a * a end

function sigmoid(a, b)
	local t = 1 + math.exp(-a * b);
	return math.abs(t) > 0.00001 and 1 / t or 0;
end

function rand(a) return math.random() * a end -- NOTE: faster than math.random(0, a) seems like it's compiled to TCALL (tailcall) which is not well-jitted

function _lte(a, b) if a <= b then return 1 else return 0 end end
function _gte(a, b) if a >= b then return 1 else return 0 end end
function _lt(a, b) if a < b then return 1 else return 0 end end
function _gt(a, b) if a > b then return 1 else return 0 end end
function _eq(a, b) if a == b then return 1 else return 0 end end
function _ne(a, b) if a ~= b then return 1 else return 0 end end

function band(a, b) if a ~= 0 and b ~= 0 then return 1 else return 0 end end
function bor(a, b) if a ~= 0 or b ~= 0 then return 1 else return 0 end end
function bnot(a) if a == 0 then return 1 else return 0 end end

--_mod = math.fmod -- NOTE: NYI!
function _mod(a, b)
	local fb = math.floor(b)
	if fb == 0 then return 0 end -- NaN
	local res = math.floor(a) % fb
	return res
end -- NOTE: this is incorrect for negative a or b

function _div(a, b)
	if b == 0.0 then return 0.0 end
	return a / b
end

local typePodFrameDataRefRef = ffi.typeof("PodFrameData**")
local typePodFrameCallbacksRefRef = ffi.typeof("PodFrameCallbacks**")
local typePodPresetFrameVarpool = ffi.typeof("PodPresetFrameVarpool")
local typePodWaveFrameVarpool = ffi.typeof("PodWaveFrameVarpool")
local typePodShapeFrameVarpool = ffi.typeof("PodShapeFrameVarpool")
local typePodPresetInputs = ffi.typeof("PodPresetInputs")

local sizePodPresetFrameVarpool = ffi.sizeof(typePodPresetFrameVarpool)
local sizePodWaveFrameVarpool = ffi.sizeof(typePodWaveFrameVarpool)
local sizePodShapeFrameVarpool = ffi.sizeof(typePodShapeFrameVarpool)
local sizePodPresetInputs = ffi.sizeof(typePodPresetInputs)

local MAX_SHAPES = 4
local MAX_WAVES = 4
local QS_LENGTH = 32
local TS_LENGTH = 8
local QS_SIZE = 4 * QS_LENGTH -- 4 == sizeof(float)
local TS_SIZE = 4 * TS_LENGTH -- 4 == sizeof(float)

local customFrameVars = {}
local customPixelVars = {}
local customShapeVars = {[0] = {}, {}, {}, {}} -- NOTE: max=4
local customWaveVars = {[0] = {}, {}, {}, {}}

local defZeroMeta = {
	__index = function(t, k) return 0 end
}

regXX = {} -- NOTE: reg[0]-reg[99]
setmetatable(regXX, defZeroMeta)

local megabufMetatable = {
	__index = function(t, k) return 0 end
,
	__newindex = function(t, k, v)
		if k ~= k or k == math.huge or k == -math.huge then
			rawset(t, 0, v)
			return
		end
		if k >= 0 then
			rawset(t, math.floor(k), v)
		end
	end
}

gmegabuf = {} -- global
setmetatable(gmegabuf, megabufMetatable)

local frameMegabuf = {}
setmetatable(frameMegabuf, megabufMetatable)

local shapeMegabufs = {[0] = {}, {}, {}, {}}
for i = 0, MAX_SHAPES - 1 do setmetatable(shapeMegabufs[i], megabufMetatable) end

local waveMegabufs = {[0] = {}, {}, {}, {}}
for i = 0, MAX_WAVES - 1 do setmetatable(waveMegabufs[i], megabufMetatable) end

local customWavePointVars = {[0] = {}, {}, {}, {}}
for i = 0, MAX_WAVES - 1 do setmetatable(customWavePointVars[i], defZeroMeta) end

__monitor = {}
setmetatable(__monitor, { __newindex = function(t, k, v) print("monitor="..v) end, __index = function(t, k) return 0 end })


function init(cFrameData)
	local frameDataRef = ffi.cast(typePodFrameDataRefRef, cFrameData)
	local frameData = frameDataRef[0]
	local p = frameData.framePool -- NOTE: not a copy, reference, as we get values back to framePool
	ffi.copy(p, frameData.preset.initialPool, sizePodPresetFrameVarpool)
	ffi.copy(p, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes pool start with PodPresetInputs layout

	frame_init_code(p, customFrameVars, frameMegabuf)
	ffi.copy(frameData.preset.initialPool.q, p.q, QS_SIZE)
	pixel_init_code(customPixelVars)

	p = frameData.shapePool
	for i = 0, MAX_SHAPES - 1 do
		local shape = frameData.shapes[i]
		if shape ~= nil then -- NOTE: checking for NULL is a bit complicated, can't be done as "if shape then", as shape is evaluated to true, thus explicit check for nil is needed
			ffi.copy(p, shape.initialPool, sizePodShapeFrameVarpool)
			ffi.copy(p.q, frameData.framePool.q, QS_SIZE) -- copy q for each wave/shape from framePool
			ffi.copy(p, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes pool start with PodPresetInputs layout
			shape_init_codes[i](p, customShapeVars[i], shapeMegabufs[i])
			ffi.copy(shape.initialPool.t, p.t, TS_SIZE)
		end
	end

	p = frameData.wavePool
	for i = 0, MAX_WAVES - 1 do
		local wave = frameData.waves[i]
		if wave ~= nil then
			ffi.copy(p, wave.initialPool, sizePodWaveFrameVarpool)
			ffi.copy(p.q, frameData.framePool.q, QS_SIZE) -- copy q for each wave/shape from framePool
			ffi.copy(p, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes pool start with PodPresetInputs layout
			wave_init_codes[i](p, customWaveVars[i], waveMegabufs[i])
			-- NOTE: then need to copy p.t to frame_data.podWavesRaw[i].initialPool.t
			ffi.copy(wave.initialPool.t, p.t, TS_SIZE)
		end
	end
end


local function perPixel(frameData, frameCallbacks)
	local pixelPool = frameData.pixelPool
	ffi.copy(pixelPool.q, frameData.framePool.q, QS_SIZE)
	ffi.copy(pixelPool, frameData.inputs, sizePodPresetInputs)

	local mesh_inputs = frameData.meshInputs
	local framePool = frameData.framePool

	local count = frameData.inputs.meshx * frameData.inputs.meshy
	for i = 0, count - 1 do
		pixelPool.x = mesh_inputs[i].orig_x  -- NOTE: we can do these assignments in c++ perPixel code, actually (once in preRender and in perPixel for each call)
		pixelPool.y = mesh_inputs[i].orig_y
		pixelPool.rad = mesh_inputs[i].orig_rad
		pixelPool.ang = mesh_inputs[i].orig_ang

		pixelPool.cx = framePool.cx
		pixelPool.cy = framePool.cy
		pixelPool.sx = framePool.sx
		pixelPool.sy = framePool.sy
		pixelPool.dx = framePool.dx
		pixelPool.dy = framePool.dy
		pixelPool.zoom = framePool.zoom
		pixelPool.zoomexp = framePool.zoomexp
		pixelPool.rot = framePool.rot
		pixelPool.warp = framePool.warp

		pixel_code(pixelPool, customPixelVars, frameMegabuf)

		frameCallbacks.perPixel(frameCallbacks.opaque, pixelPool, i)
	end
end


local function perPoints(ix, frameData, frameCallbacks, samples, pointPool, framePool, wave)
	local waveDataL
	--local waveDataR
	local mult

	ffi.copy(pointPool.q, framePool.q, QS_SIZE)
	ffi.copy(pointPool.t, framePool.t, TS_SIZE)
	ffi.copy(pointPool, frameData.inputs, sizePodPresetInputs)

	if not wave.bSpectrum then
		waveDataL = frameData.pcmdata_L
		--waveDataR = frameData.pcmdata_R
		local CUSTOM_WAVE_MILKDROP_SAMPLE_SCALING = 128.0
		mult = 0.004 * CUSTOM_WAVE_MILKDROP_SAMPLE_SCALING
	else
		waveDataL = frameData.fftdata
		--waveDataR = frameData.fftdata
		mult = 1.0
	end

	mult = mult * wave.scaling * frameData.preset.fWaveScale

	-- DIFF: milkdrop does forward + backward smoothing then mult. We do only forward smoothing, thus coefs changed. There is slight visual difference on smooth=1.0
	local mix1 = math.pow(wave.smoothing * .97, 0.5)
	local mix2 = 1.0 - mix1

	local j_mult = 1.0 / (samples - 1)

	local prevL = waveDataL[0]
	--local prevR = waveDataR[0]

	for i = 0, samples - 1 do
		pointPool.value1 = prevL * mult
		--pointPool.value2 = prevR * mult
		pointPool.value2 = pointPool.value1
		pointPool.sample = i * j_mult

		pointPool.r = framePool.r
		pointPool.g = framePool.g
		pointPool.b = framePool.b
		pointPool.a = framePool.a
		pointPool.x = 0.5 + pointPool.value1
		pointPool.y = 0.5 + pointPool.value2

		wave_point_codes[ix](pointPool, customWavePointVars[ix], waveMegabufs[ix]) -- DIFF: we share wave[ix] vars
		frameCallbacks.perPoint(frameCallbacks.opaque, pointPool, i)
		prevL = waveDataL[i] * mix2 + prevL * mix1
		--prevR = waveDataR[i] * mix2 + prevR * mix1
	end
end


local function renderShape(p, i, shape, frameData, frameCallbacks)
	local num_inst = shape.initialPool.num_inst -- NOTE: important to get from initialPool as we don't accept writes to num_inst

	if num_inst > frameData.preset.maxShapeInstances then num_inst = frameData.preset.maxShapeInstances
	elseif num_inst < 0 then num_inst = 1 end

	for inst = 0, num_inst - 1 do
		ffi.copy(p, shape.initialPool, sizePodShapeFrameVarpool) -- ts, qs, inputs included
		p.instance = inst
		shape_frame_codes[i](p, customShapeVars[i], shapeMegabufs[i])
		frameCallbacks.renderShape(frameCallbacks.opaque, shape, p)
	end
end


function render(cFrameData, cFrameCallbacks)
	local frameDataRef = ffi.cast(typePodFrameDataRefRef, cFrameData)
	local frameData = frameDataRef[0]
	local frameCallbacksRef = ffi.cast(typePodFrameCallbacksRefRef, cFrameCallbacks)
	local frameCallbacks = frameCallbacksRef[0]

	local p = frameData.framePool
	ffi.copy(p, frameData.preset.initialPool, sizePodPresetFrameVarpool) -- qs included
	ffi.copy(p, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes pool start with PodPresetInputs layout
	frame_code(p, customFrameVars, frameMegabuf)

	perPixel(frameData, frameCallbacks)
	frameCallbacks.renderPixels(frameCallbacks.opaque, frameData)

	p = frameData.shapePool
	for i = 0, MAX_SHAPES - 1 do
		local shape = frameData.shapes[i]
		if shape ~= nil then
			-- copy everything into shape's initialPool, we then copy initialPool to each shape/instance pool
			ffi.copy(shape.initialPool, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes shape pool start with PodPresetInputs layout
			ffi.copy(shape.initialPool.q, frameData.framePool.q, QS_SIZE) -- copy q for each wave/shape from framePool

			renderShape(p, i, shape, frameData, frameCallbacks)
		end
	end

	p = frameData.wavePool
	local pp = frameData.pointPool
	for i = 0, MAX_WAVES - 1 do
		local wave = frameData.waves[i]
		if wave ~= nil then
			ffi.copy(p, wave.initialPool, sizePodWaveFrameVarpool) -- ts included
			ffi.copy(p.q, frameData.framePool.q, QS_SIZE) -- copy q for each wave/shape from framePool
			ffi.copy(p, frameData.inputs, sizePodPresetInputs) -- NOTE: assumes pool start with PodPresetInputs layout

			wave_frame_codes[i](p, customWaveVars[i], waveMegabufs[i])

			local samples = min(512, p.samples)
			if samples >= 2 or wave.bUseDots ~= 0 and samples >= 1 then
				perPoints(i, frameData, frameCallbacks, samples, pp, p, wave)
				frameCallbacks.renderWave(frameCallbacks.opaque, wave, samples)
			end
		end
	end

	-- NOTE: simple wave, etc. is rendered by cpp code, as we don't need to intermix lua with cpp code anymore
end



 