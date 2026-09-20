package com.skb.music.player

import androidx.compose.runtime.mutableStateOf

/**
 * Hi-Res audio pipeline configuration.
 * Runtime toggles for 32-bit float + Nx upsampling.
 */
object HiResSettings {

    // 32-bit float internal processing
    val float32 = mutableStateOf(true)

    // Upsample factor: 1x (off), 2x, 4x, 8x
    val factor = mutableStateOf(4)

    // Overall on/off
    val enabled = mutableStateOf(true)

    fun effectiveFactor(): Int = if (enabled.value) factor.value else 1

    /**
     * Given input sample rate, what's the output rate?
     * Example: 48kHz * 4 = 192kHz, 96kHz * 8 = 768kHz
     */
    fun outputRate(inputRate: Int): Int =
        (inputRate * effectiveFactor()).coerceAtMost(768_000)

    /** Human label */
    fun label(inputRate: Int): String {
        val out = outputRate(inputRate)
        return "$out Hz · 32-bit float · ${effectiveFactor()}x"
    }
}
