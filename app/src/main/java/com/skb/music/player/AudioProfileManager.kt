package com.skb.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.media3.exoplayer.ExoPlayer

/**
 * Master DSP pipeline for SKB Music.
 * Chaines: BassBoost → Virtualizer → Equalizer → LoudnessEnhancer
 */
object AudioProfileManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var sessionId: Int = 0

    // Current profile
    var activeHeadphone: String = "None"
    var activeGenre: String = "Flat"
    var bassLevel: Float = 0.5f
    var virtualLevel: Float = 0.3f
    var loudnessGain: Int = 0      // in millibels (0..2000)
    var eqEnabled: Boolean = true

    // Genre → per-band gains (dB, 5 bands)
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

    // ═══════════════════════════════════════════════════
    fun attach(player: ExoPlayer) {
        sessionId = player.audioSessionId
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudness?.release() }

        runCatching {
            equalizer = Equalizer(0, sessionId).apply { enabled = eqEnabled }
        }
        runCatching {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
            setBassStrength(bassLevel)
        }
        runCatching {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
            setVirtualStrength(virtualLevel)
        }
        runCatching {
            loudness = LoudnessEnhancer(sessionId).apply {
                enabled = loudnessGain > 0
                setTargetGain(loudnessGain)
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // Genre EQ
    // ═══════════════════════════════════════════════════
    fun applyGenre(genre: String) {
        activeGenre = genre
        val eq = equalizer ?: return
        val gains = genreGains[genre] ?: genreGains["Flat"]!!
        val range = eq.bandLevelRange
        val min = range[0]; val max = range[1]
        val bands = eq.numberOfBands.toInt()
        for (b in 0 until bands) {
            val g = gains.getOrElse(b) { 0f }
            val level = (g / 12f * (max - min) / 2f + (max + min) / 2f).toInt().toShort()
            runCatching { eq.setBandLevel(b.toShort(), level) }
        }
    }

    // ═══════════════════════════════════════════════════
    // AutoEq (custom bands, from DB)
    // ═══════════════════════════════════════════════════
    fun applyAutoEqBands(headphoneName: String, gainsDb: List<Float>) {
        activeHeadphone = headphoneName
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val min = range[0]; val max = range[1]
        val bands = eq.numberOfBands.toInt()
        for (b in 0 until bands) {
            val g = gainsDb.getOrElse(b) { 0f }
            val level = (g / 12f * (max - min) / 2f + (max + min) / 2f).toInt().toShort()
            runCatching { eq.setBandLevel(b.toShort(), level) }
        }
    }

    // ═══════════════════════════════════════════════════
    fun setBassStrength(v: Float) {
        bassLevel = v.coerceIn(0f, 1f)
        runCatching { bassBoost?.setStrength(bassLevel.toShort()) }
    }

    fun setVirtualStrength(v: Float) {
        virtualLevel = v.coerceIn(0f, 1f)
        runCatching { virtualizer?.setStrength(virtualLevel.toShort()) }
    }

    fun setLoudnessGain(mb: Int) {
        loudnessGain = mb.coerceIn(0, 2000)
        runCatching {
            loudness?.enabled = loudnessGain > 0
            loudness?.setTargetGain(loudnessGain)
        }
    }

    fun toggleEq(on: Boolean) {
        eqEnabled = on
        runCatching { equalizer?.enabled = on }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudness?.release() }
        equalizer = null; bassBoost = null; virtualizer = null; loudness = null
    }

    /** Returns current audio output info */
    fun currentInfo(): AudioInfo {
        val eq = equalizer
        val numBands = eq?.numberOfBands?.toInt() ?: 0
        val centerFreqs = eq?.let {
            (0 until it.numberOfBands.toInt()).map { b ->
                (it.getCenterFreq(b.toShort()) / 1000).toString() + " Hz"
            }
        } ?: emptyList()
        return AudioInfo(
            sessionId = sessionId,
            bands = numBands,
            centerFreqs = centerFreqs,
            headphone = activeHeadphone,
            genre = activeGenre,
            bassBoost = (bassLevel * 100).toInt(),
            virtualizer = (virtualLevel * 100).toInt(),
            loudnessGain = loudnessGain,
            eqOn = eqEnabled
        )
    }

    data class AudioInfo(
        val sessionId: Int,
        val bands: Int,
        val centerFreqs: List<String>,
        val headphone: String,
        val genre: String,
        val bassBoost: Int,
        val virtualizer: Int,
        val loudnessGain: Int,
        val eqOn: Boolean
    )
}
