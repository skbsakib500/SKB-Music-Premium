package com.skb.music.player

import android.media.audiofx.Equalizer
import androidx.media3.exoplayer.ExoPlayer

/**
 * Wraps Android's built-in Equalizer effect for our ExoPlayer audio session.
 */
object EqualizerManager {
    private var equalizer: Equalizer? = null
    private var currentPreset: String = "Flat"

    val presets = listOf(
        "Flat", "Rock", "Pop", "Jazz", "Classical",
        "Bass Boost", "Treble Boost", "Vocal"
    )

    fun attach(player: ExoPlayer) {
        runCatching {
            equalizer?.release()
            equalizer = Equalizer(0, player.audioSessionId).apply {
                enabled = true
            }
        }
    }

    fun applyPreset(name: String) {
        currentPreset = name
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val min = range[0]
        val max = range[1]

        val bands = eq.numberOfBands.toInt()
        val gains = when (name) {
            "Rock"          -> listOf(4f, 3f, -1f, 2f, 4f)
            "Pop"           -> listOf(-1f, 1f, 3f, 2f, -1f)
            "Jazz"          -> listOf(3f, 2f, -1f, 1f, 3f)
            "Classical"     -> listOf(4f, 3f, -1f, 3f, 4f)
            "Bass Boost"    -> listOf(6f, 5f, 2f, 0f, 0f)
            "Treble Boost"  -> listOf(0f, 0f, 2f, 5f, 6f)
            "Vocal"         -> listOf(-2f, 0f, 4f, 3f, 1f)
            else            -> listOf(0f, 0f, 0f, 0f, 0f)
        }

        for (b in 0 until bands) {
            val g = gains.getOrElse(b) { 0f }
            val level = (g / 6f * (max - min) / 2f + (max + min) / 2f).toInt().toShort()
            runCatching { eq.setBandLevel(b.toShort(), level) }
        }
    }

    fun current(): String = currentPreset

    fun release() {
        runCatching { equalizer?.release() }
        equalizer = null
    }
}
