package com.skb.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * Attaches Android's built-in AudioEffect chain to a specific session ID.
 * Call attach(sessionId) AFTER ExoPlayer gives us a real session.
 */
object AudioProfileManager {
    private const val TAG = "SKB-AudioProfile"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var presetReverb: PresetReverb? = null

    private var activeSession = 0

    var activeGenre: String = "Flat"
        private set
    var bassLevel: Float = 0.5f
        private set
    var virtualLevel: Float = 0.3f
        private set
    var loudnessMb: Int = 0
        private set
    var eqEnabled: Boolean = true
        private set

    /** True when effects were successfully created */
    var isAttached: Boolean = false
        private set

    private val genreGains = mapOf(
        "Flat"      to listOf(0f, 0f, 0f, 0f, 0f),
        "Rock"      to listOf(4f, 3f, -1f, 2f, 4f),
        "Pop"       to listOf(-1f, 1f, 3f, 2f, -1f),
        "Jazz"      to listOf(3f, 2f, -1f, 1f, 3f),
        "Classical" to listOf(4f, 3f, -1f, 3f, 4f),
        "EDM"       to listOf(6f, 4f, -2f, 3f, 5f),
        "Hip-Hop"   to listOf(6f, 5f, 1f, 0f, 1f),
        "Vocal"     to listOf(-2f, 0f, 4f, 3f, 1f),
        "Bass"      to listOf(7f, 6f, 2f, 0f, 0f),
        "Treble"    to listOf(0f, 0f, 2f, 5f, 7f)
    )
    val genres: List<String> = genreGains.keys.toList()

    /**
     * Attach all effects to a session ID. Safe to call multiple times.
     */
    fun attach(sessionId: Int) {
        if (sessionId == 0) {
            Log.w(TAG, "attach() called with sessionId=0 — ignored")
            return
        }
        if (isAttached && activeSession == sessionId) {
            return  // already good
        }
        release()
        activeSession = sessionId
        Log.i(TAG, "Attaching effects to session $sessionId")

        runCatching {
            equalizer = Equalizer(0, sessionId).apply { enabled = eqEnabled }
        }.onFailure { Log.w(TAG, "EQ init failed: ${it.message}") }

        runCatching {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
            setBassStrength(bassLevel)
        }.onFailure { Log.w(TAG, "Bass init failed: ${it.message}") }

        runCatching {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
            setVirtualStrength(virtualLevel)
        }.onFailure { Log.w(TAG, "Virt init failed: ${it.message}") }

        runCatching {
            loudness = LoudnessEnhancer(sessionId).apply {
                enabled = loudnessMb > 0
                setTargetGain(loudnessMb)
            }
        }.onFailure { Log.w(TAG, "Loudness init failed: ${it.message}") }

        runCatching {
            presetReverb = PresetReverb(0, sessionId)
        }.onFailure { Log.w(TAG, "Reverb init failed: ${it.message}") }

        applyGenre(activeGenre)
        isAttached = equalizer != null || bassBoost != null || virtualizer != null
    }

    private fun mapLevel(gainDb: Float, minL: Int, maxL: Int, scaleDb: Float = 12f): Short {
        val mid = (minL + maxL) / 2
        val halfSpan = (maxL - minL) / 2
        val scaled = (gainDb / scaleDb * halfSpan + mid).toInt()
        return scaled.toShort()
    }

    fun applyGenre(genre: String) {
        activeGenre = genre
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val minL = range[0].toInt()
        val maxL = range[1].toInt()
        val gains = genreGains[genre] ?: return
        val bands = eq.numberOfBands.toInt()
        for (b in 0 until bands) {
            val g = gains.getOrElse(b) { 0f }
            runCatching { eq.setBandLevel(b.toShort(), mapLevel(g, minL, maxL)) }
        }
    }

    fun setBassStrength(v: Float) {
        bassLevel = v.coerceIn(0f, 1f)
        runCatching { bassBoost?.setStrength((bassLevel * 1000f).toInt().toShort()) }
    }

    fun setVirtualStrength(v: Float) {
        virtualLevel = v.coerceIn(0f, 1f)
        runCatching { virtualizer?.setStrength((virtualLevel * 1000f).toInt().toShort()) }
    }

    fun applyLoudnessGain(mb: Int) {
        loudnessMb = mb.coerceIn(0, 2000)
        runCatching {
            loudness?.enabled = loudnessMb > 0
            loudness?.setTargetGain(loudnessMb)
        }
    }

    fun toggleEq(on: Boolean) {
        eqEnabled = on
        runCatching { equalizer?.enabled = on }
    }

    fun setReverbPreset(preset: Int) {
        runCatching {
            presetReverb?.enabled = preset >= 0
            if (preset >= 0) presetReverb?.preset = preset.toShort()
        }
    }

    fun release() {
        runCatching { equalizer?.release() }; equalizer = null
        runCatching { bassBoost?.release() }; bassBoost = null
        runCatching { virtualizer?.release() }; virtualizer = null
        runCatching { loudness?.release() }; loudness = null
        runCatching { presetReverb?.release() }; presetReverb = null
        isAttached = false
        activeSession = 0
    }
}
