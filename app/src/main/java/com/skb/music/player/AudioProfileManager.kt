package com.skb.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.media3.exoplayer.ExoPlayer

object AudioProfileManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var sessionId: Int = 0

    var activeHeadphone: String = "None"
    var activeGenre: String = "Flat"
    var bassLevel: Float = 0.5f
    var virtualLevel: Float = 0.3f
    var loudnessGain: Int = 0
    var eqEnabled: Boolean = true

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

    /**
     * Maps a dB gain into the equalizer's native level range.
     * Uses explicit Int → Short conversion to avoid Kotlin ambiguity.
     */
    private fun mapLevel(
        gainDb: Float,
        rangeMin: Int,
        rangeMax: Int,
        scaleDb: Float = 12f
    ): Short {
        val mid = (rangeMin + rangeMax) / 2
        val halfSpan = (rangeMax - rangeMin) / 2
        val scaled: Int = (gainDb / scaleDb * halfSpan.toFloat() + mid.toFloat()).toInt()
        return scaled.toShort()
    }

    fun applyGenre(genre: String) {
        activeGenre = genre
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val minL = range[0].toInt()
        val maxL = range[1].toInt()
        val gains = genreGains[genre] ?: genreGains["Flat"]!!
        val bands = eq.numberOfBands.toInt()
        for (b in 0 until bands) {
            val g = gains.getOrElse(b) { 0f }
            val level = mapLevel(g, minL, maxL, 12f)
            runCatching { eq.setBandLevel(b.toShort(), level) }
        }
    }

    fun applyAutoEqBands(headphoneName: String, gainsDb: List<Float>) {
        activeHeadphone = headphoneName
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val minL = range[0].toInt()
        val maxL = range[1].toInt()
        val bands = eq.numberOfBands.toInt()
        for (b in 0 until bands) {
            val g = gainsDb.getOrElse(b) { 0f }
            val level = mapLevel(g, minL, maxL, 12f)
            runCatching { eq.setBandLevel(b.toShort(), level) }
        }
    }

    fun setBassStrength(v: Float) {
        bassLevel = v.coerceIn(0f, 1f)
        val s: Short = (bassLevel * 1000f).toInt().toShort()
        runCatching { bassBoost?.setStrength(s) }
    }

    fun setVirtualStrength(v: Float) {
        virtualLevel = v.coerceIn(0f, 1f)
        val s: Short = (virtualLevel * 1000f).toInt().toShort()
        runCatching { virtualizer?.setStrength(s) }
    }

    fun applyLoudnessGain(mb: Int) {
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

    fun currentInfo(): AudioInfo {
        val eq = equalizer
        val numBands = eq?.numberOfBands?.toInt() ?: 0
        val centerFreqs = eq?.let {
            (0 until it.numberOfBands.toInt()).map { b ->
                (it.getCenterFreq(b.toShort()) / 1000).toString() + " Hz"
            }
        } ?: emptyList()
        return AudioInfo(
            sessionId, numBands, centerFreqs,
            activeHeadphone, activeGenre,
            (bassLevel * 100).toInt(),
            (virtualLevel * 100).toInt(),
            loudnessGain, eqEnabled
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
