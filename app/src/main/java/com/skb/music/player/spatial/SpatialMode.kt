package com.skb.music.player.spatial

import androidx.compose.runtime.mutableStateOf

enum class SpatialType {
    OFF, SPATIAL_8D, SPATIAL_10D, BINAURAL_3D
}

object SpatialMode {

    val enabled = mutableStateOf(false)
    val type    = mutableStateOf(SpatialType.OFF)

    // 8D params
    val rotateSpeed   = mutableStateOf(0.08f)   // Hz
    val rotateWet     = mutableStateOf(0.25f)
    val rotateRadius  = mutableStateOf(1.0f)

    // 10D params
    val hyperSpeed    = mutableStateOf(0.05f)
    val hyperWet      = mutableStateOf(0.35f)
    val hyperDepth    = mutableStateOf(0.80f)
    val hyperHaas     = mutableStateOf(12f)

    // 3D binaural params
    val binauralWidth = mutableStateOf(0.70f)
    val binauralDepth = mutableStateOf(0.50f)
    val binauralElev  = mutableStateOf(0.0f)

    fun setType(t: SpatialType) {
        type.value = t
        enabled.value = t != SpatialType.OFF
    }

    fun label(): String = when (type.value) {
        SpatialType.OFF -> "Off"
        SpatialType.SPATIAL_8D -> "8D Rotating"
        SpatialType.SPATIAL_10D -> "10D Hyper"
        SpatialType.BINAURAL_3D -> "3D Binaural"
    }
}
