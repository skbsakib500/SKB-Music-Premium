package com.skb.music.player

import androidx.compose.runtime.mutableStateOf

/**
 * Global audio pipeline settings. UI থেকে বদলালে rebuild লাগবে
 * (service restart)।
 */
object AudioPipeline {

    // ── Master switches ──
    val dspEnabled   = mutableStateOf(true)

    // ── Module values ──
    val preampDb     = mutableStateOf(0f)       // -12..+12
    val headroomDb   = mutableStateOf(-1f)      // -6..0
    val stereoWidth  = mutableStateOf(1.0f)     // 0..2
    val crossfeed    = mutableStateOf(0.3f)     // 0..1
    val dynBass      = mutableStateOf(0.4f)     // 0..1
    val dynBassGain  = mutableStateOf(3f)       // 0..12 dB
    val exciter      = mutableStateOf(0.2f)     // 0..1
    val limiterOn    = mutableStateOf(true)
    val limiterCeil  = mutableStateOf(0.95f)    // 0.5..1

    // ── Presets ──
    data class Preset(
        val name: String,
        val preamp: Float,
        val headroom: Float,
        val width: Float,
        val crossfeed: Float,
        val bass: Float,
        val bassGain: Float,
        val exciter: Float,
        val limiter: Boolean
    )

    val presets = listOf(
        Preset("Reference",      0f,  -1f, 1.0f, 0.0f, 0.0f,  0f, 0.0f, true),
        Preset("Hi-Res Studio",  0f,  -2f, 1.15f, 0.15f, 0.15f, 2f, 0.15f, true),
        Preset("Bass Head",      0f,  -1f, 1.1f, 0.25f, 0.85f, 8f, 0.25f, true),
        Preset("Vocal Clarity",  1f,  -1f, 1.0f, 0.3f, 0.1f,  0f, 0.6f, true),
        Preset("Night Mode",    -4f,  -1f, 1.0f, 0.4f, 0.4f,  2f, 0.1f, true),
        Preset("Cinema",         1f,  -2f, 1.3f, 0.2f, 0.6f,  5f, 0.3f, true),
        Preset("Wide Stage",     0f,  -1f, 1.6f, 0.5f, 0.2f,  2f, 0.2f, true),
        Preset("Purist Bypass",  0f,  0f,  1.0f, 0.0f, 0.0f,  0f, 0.0f, false)
    )

    fun applyPreset(p: Preset) {
        preampDb.value = p.preamp
        headroomDb.value = p.headroom
        stereoWidth.value = p.width
        crossfeed.value = p.crossfeed
        dynBass.value = p.bass
        dynBassGain.value = p.bassGain
        exciter.value = p.exciter
        limiterOn.value = p.limiter
    }

    fun snapshot(): Map<String, Float> = mapOf(
        "preamp" to preampDb.value,
        "headroom" to headroomDb.value,
        "width" to stereoWidth.value,
        "crossfeed" to crossfeed.value,
        "bass" to dynBass.value,
        "bassGain" to dynBassGain.value,
        "exciter" to exciter.value,
        "limiter" to if (limiterOn.value) 1f else 0f
    )
}
