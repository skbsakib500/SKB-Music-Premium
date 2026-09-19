local ffi = require("ffi")
ffi.cdef[[
// from asm/posix_types.h:
typedef long __kernel_off_t;

// from sys/types.h:
typedef __kernel_off_t off_t;

// from android/asset_manager.h:

struct AAssetManager;
typedef struct AAssetManager AAssetManager;

struct AAssetDir;
typedef struct AAssetDir AAssetDir;

struct AAsset;
typedef struct AAsset AAsset;

enum {
    AASSET_MODE_UNKNOWN      = 0,
    AASSET_MODE_RANDOM       = 1,
    AASSET_MODE_STREAMING    = 2,
    AASSET_MODE_BUFFER       = 3
};

AAssetDir* AAssetManager_openDir(AAssetManager* mgr, const char* dirName);
AAsset* AAssetManager_open(AAssetManager* mgr, const char* filename, int mode);
const char* AAssetDir_getNextFileName(AAssetDir* assetDir);
void AAssetDir_rewind(AAssetDir* assetDir);
void AAssetDir_close(AAssetDir* assetDir);
int AAsset_read(AAsset* asset, void* buf, size_t count);
off_t AAsset_seek(AAsset* asset, off_t offset, int whence);
void AAsset_close(AAsset* asset);
const void* AAsset_getBuffer(AAsset* asset);
off_t AAsset_getLength(AAsset* asset);
off_t AAsset_getRemainingLength(AAsset* asset);
int AAsset_openFileDescriptor(AAsset* asset, off_t* outStart, off_t* outLength);
int AAsset_isAllocated(AAsset* asset);
]]

-- Android specific

local android = {
    assetManager = nil,
}

--[[
a loader function for Lua which will look for assets when loading modules
--]]
function android.asset_loader(modulename)
    local errmsg = ""
    -- Find source
    local modulepath = "milk/lua/"..string.gsub(modulename, "%.", "/")
    local filename = string.gsub("?.lua", "%?", modulepath)
    local asset = ffi.C.AAssetManager_open(
        android.assetManager,
        filename, ffi.C.AASSET_MODE_BUFFER)
    if asset ~= nil then
        -- read asset:
        local assetdata = ffi.C.AAsset_getBuffer(asset)
        local assetsize = ffi.C.AAsset_getLength(asset)
        if assetdata ~= nil then
            -- Compile and return the module
            local compiled = assert(loadstring(ffi.string(assetdata, assetsize), filename))
            ffi.C.AAsset_close(asset)
            return compiled
        else
            ffi.C.AAsset_close(asset)
            errmsg = errmsg.."\n\tunaccessible file '"..filename.."' (tried with asset loader)"
        end
    else
        errmsg = errmsg.."\n\tno file '"..filename.."' (checked with asset loader)"
    end
    return errmsg
end

--[[
the C code will call this function:
--]]
local function run(assetManager)
	if assetManager ~= nil then
		android.assetManager = assetManager
	    -- register the "android" module (ourself)
	    --package.loaded.android = android
	
	    -- set up a sensible package.path
	    package.path = "?.lua"
	    -- register the asset loader
	    table.insert(package.loaders, 2, android.asset_loader)
	else
		print("DEBUG: no assetManager")
	end	    
end

run(...)

local jitv = require "jit.v"

local androidlog_stream = { 
	write = function(self, str)
		A.LOGW("jit.v", tostring(str))
	end,
	flush = function() end,
	close = function() end,
}

jitv.start(androidlog_stream)

A.LOGE(TAG, "LUAJIT DEBUG!!!")
