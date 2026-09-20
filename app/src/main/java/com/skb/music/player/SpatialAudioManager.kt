package com.skb.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import androidx.media3.exoplayer.ExoPlayer

/**
 * Spatial audio using Android's native AudioEffect API.
 * Works on every device without custom AudioProcessor.
 *
 * Modes:
 *  OFF     - bypass
 *  ROTATE  - 8D-like: strong virtualizer + rotating reverb
 *  HYPER   - 10D: heavier reverb + deep virtualizer + bass
 *  BINAURAL- 3D: virtualizer + room reverb (headphone only)
 */
object SpatialAudioManager {

    enum class Mode { OFF, ROTATE_8D, HYPER_10D, BINAURAL_3D }

    var currentMode: Mode = Mode.OFF
    var intensity: Float = 0.6f   // 0..1

    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var envReverb: EnvironmentalReverb? = null
    private var bassBoost: BassBoost? = null

    private var sessionId: Int = 0

    fun attach(player: ExoPlayer) {
        sessionId = player.audioSessionId
        releaseAll()

        runCatching { virtualizer = Virtualizer(0, sessionId) }
        runCatching { presetReverb = PresetReverb(0, sessionId) }
        runCatching { envReverb = EnvironmentalReverb(0, sessionId) }
        runCatching { bassBoost = BassBoost(0, sessionId) }

        apply(currentMode)
    }

    fun apply(mode: Mode) {
        currentMode = mode
        val i = intensity.coerceIn(0f, 1f)

        // Start clean
        runCatching { virtualizer?.enabled = false }
        runCatching { presetReverb?.enabled = false }
        runCatching { envReverb?.enabled = false }
        runCatching { bassBoost?.enabled = false }

        when (mode) {
            Mode.OFF -> { /* all disabled */ }

            Mode.ROTATE_8D -> {
                // Strong virtualizer + medium room = "walking around" feel
                runCatching {
                    virtualizer?.enabled = true
                    virtualizer?.setStrength((i * 1000).toInt().toShort())
                }
                runCatching {
                    presetReverb?.enabled = true
                    // PRESET_MEDIUMROOM = 3
                    presetReverb?.preset = 3
                }
            }

            Mode.HYPER_10D -> {
                // Max virtualizer + large room + bass emphasis
                runCatching {
                    virtualizer?.enabled = true
                    virtualizer?.setStrength((i * 1000).toInt().toShort())
                }
                runCatching {
                    envReverb?.enabled = true
                    envReverb?.roomLevel = (-1000 + i * 1500).toInt().toShort()
                    envReverb?.decayTime = (1500 + i * 2500).toInt()
                    envReverb?.reverbLevel = (-1500 + i * 1800).toInt().toShort()
                    envReverb?.diffusion = (i * 1000).toInt().toShort()
                    envReverb?.density = (i * 1000).toInt().toShort()
                }
                runCatching {
                    bassBoost?.enabled = true
                    bassBoost?.setStrength((i * 800).toInt().toShort())
                }
            }

            Mode.BINAURAL_3D -> {
                // Mid virtualizer + small room = intimate 3D
                runCatching {
                    virtualizer?.enabled = true
                    virtualizer?.setStrength((i * 600).toInt().toShort())
                }
                runCatching {
                    presetReverb?.enabled = true
                    // PRESET_SMALLROOM = 1
                    presetReverb?.preset = 1
                }
            }
        }
    }

    fun setIntensity(v: Float) {
        intensity = v.coerceIn(0f, 1f)
        apply(currentMode)
    }

    fun releaseAll() {
        runCatching { virtualizer?.release() }; virtualizer = null
        runCatching { presetReverb?.release() }; presetReverb = null
        runCatching { envReverb?.release() }; envReverb = null
        runCatching { bassBoost?.release() }; bassBoost = null
    }

    fun release() = releaseAll()
}
