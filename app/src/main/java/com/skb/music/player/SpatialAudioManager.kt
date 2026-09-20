package com.skb.music.player

import android.util.Log

/**
 * Spatial modes are built by combining AudioProfileManager's effects:
 *  8D  = strong Virtualizer + MediumRoom reverb
 *  10D = strong Virtualizer + LargeRoom reverb + Bass
 *  3D  = moderate Virtualizer + SmallRoom reverb
 */
object SpatialAudioManager {
    private const val TAG = "SKB-Spatial"

    enum class Mode { OFF, ROTATE_8D, HYPER_10D, BINAURAL_3D }

    var currentMode: Mode = Mode.OFF
        private set
    var level: Float = 0.6f
        private set

    fun apply(mode: Mode) {
        currentMode = mode
        if (!AudioProfileManager.isAttached) {
            Log.w(TAG, "AudioProfileManager not attached yet — mode saved, will apply later")
            return
        }
        val i = level
        when (mode) {
            Mode.OFF -> {
                AudioProfileManager.setVirtualStrength(0f)
                AudioProfileManager.setReverbPreset(-1)
            }
            Mode.ROTATE_8D -> {
                AudioProfileManager.setVirtualStrength(i)
                AudioProfileManager.setReverbPreset(3)  // MEDIUMROOM
            }
            Mode.HYPER_10D -> {
                AudioProfileManager.setVirtualStrength(i)
                AudioProfileManager.setReverbPreset(4)  // LARGEROOM
                AudioProfileManager.setBassStrength((i * 0.9f).coerceAtMost(1f))
            }
            Mode.BINAURAL_3D -> {
                AudioProfileManager.setVirtualStrength((i * 0.6f))
                AudioProfileManager.setReverbPreset(1)  // SMALLROOM
            }
        }
    }

    fun applyLevel(v: Float) {
        level = v.coerceIn(0f, 1f)
        apply(currentMode)
    }

    fun release() {
        currentMode = Mode.OFF
    }
}
