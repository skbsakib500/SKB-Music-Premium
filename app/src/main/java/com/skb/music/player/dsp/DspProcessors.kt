package com.skb.music.player.dsp

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.tanh

// ═══════════════════════════════════════════════════════
// Base processor — 32-bit float in/out
// ═══════════════════════════════════════════════════════
@UnstableApi
abstract class FloatProcessor : AudioProcessor {
    protected var channels = 2
    protected var sampleRate = 48000
    protected var active = false
    private var outBuf: ByteBuffer = AudioProcessor.EMPTY_BUFFER

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        channels = inputAudioFormat.channelCount
        sampleRate = inputAudioFormat.sampleRate
        active = true
        // Always output 32-bit float
        return AudioProcessor.AudioFormat(
            C.ENCODING_PCM_FLOAT,
            inputAudioFormat.channelCount,
            inputAudioFormat.sampleRate
        )
    }

    override fun isActive(): Boolean = active
    override fun isEnded(): Boolean = false

    override fun queueInput(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.nativeOrder())
        if (!inputBuffer.hasRemaining()) {
            outBuf = AudioProcessor.EMPTY_BUFFER
            return
        }
        val n = inputBuffer.remaining() / 4  // float = 4 bytes
        val samples = FloatArray(n)
        inputBuffer.asFloatBuffer().get(samples)
        inputBuffer.position(inputBuffer.limit())

        process(samples)

        val out = ByteBuffer.allocate(samples.size * 4).order(ByteOrder.nativeOrder())
        out.asFloatBuffer().put(samples)
        out.flip()
        outBuf = out
    }

    override fun queueEndOfStream() {}

    override fun getOutput(): ByteBuffer {
        val o = outBuf
        outBuf = AudioProcessor.EMPTY_BUFFER
        return o
    }

    override fun flush() { outBuf = AudioProcessor.EMPTY_BUFFER }

    override fun reset() {
        flush()
        active = false
        onReset()
    }

    protected abstract fun process(samples: FloatArray)
    protected open fun onReset() {}
}

// ═══════════════════════════════════════════════════════
// Preamp — gain + soft headroom
// ═══════════════════════════════════════════════════════
@UnstableApi
class PreampProcessor(
    @Volatile var gainDb: Float = 0f,
    @Volatile var headroomDb: Float = -1f
) : FloatProcessor() {

    override fun process(samples: FloatArray) {
        val gain = dbToLinear(gainDb + headroomDb)
        for (i in samples.indices) {
            samples[i] = samples[i] * gain
        }
    }
}

// ═══════════════════════════════════════════════════════
// Stereo Width — M/S matrix
// ═══════════════════════════════════════════════════════
@UnstableApi
class StereoWidthProcessor(
    @Volatile var width: Float = 1.0f     // 0..2
) : FloatProcessor() {

    override fun process(samples: FloatArray) {
        if (channels < 2) return
        val w = width.coerceIn(0f, 2f)
        var i = 0
        while (i + 1 < samples.size) {
            val l = samples[i]
            val r = samples[i + 1]
            val m = (l + r) * 0.5f
            val s = (l - r) * 0.5f * w
            samples[i] = m + s
            samples[i + 1] = m - s
            i += 2
        }
    }
}

// ═══════════════════════════════════════════════════════
// Crossfeed — Bauer style, natural headphone image
// ═══════════════════════════════════════════════════════
@UnstableApi
class CrossfeedProcessor(
    @Volatile var amount: Float = 0.3f,   // 0..1
    @Volatile var cutoffHz: Float = 700f
) : FloatProcessor() {

    private var lastL = 0f
    private var lastR = 0f
    private var alpha = 0.1f

    override fun configure(input: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val r = super.configure(input)
        recomputeAlpha()
        return r
    }

    private fun recomputeAlpha() {
        val dt = 1.0 / sampleRate
        val rc = 1.0 / (2.0 * Math.PI * cutoffHz)
        alpha = (dt / (rc + dt)).toFloat()
    }

    override fun process(samples: FloatArray) {
        if (channels < 2) return
        val a = amount.coerceIn(0f, 1f)
        var i = 0
        while (i + 1 < samples.size) {
            val l = samples[i]
            val r = samples[i + 1]
            // One-pole low-pass on opposite channel
            lastL += alpha * (l - lastL)
            lastR += alpha * (r - lastR)
            samples[i]     = l + a * lastR * 0.5f
            samples[i + 1] = r + a * lastL * 0.5f
            i += 2
        }
    }

    override fun onReset() {
        lastL = 0f
        lastR = 0f
    }
}

// ═══════════════════════════════════════════════════════
// Dynamic Bass — freq-split + compression on lows
// ═══════════════════════════════════════════════════════
@UnstableApi
class DynamicBassProcessor(
    @Volatile var amount: Float = 0.4f,    // 0..1
    @Volatile var bassGainDb: Float = 3f
) : FloatProcessor() {

    private val lpL = OnePole()
    private val lpR = OnePole()
    private var envL = 0f
    private var envR = 0f

    override fun configure(input: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val r = super.configure(input)
        lpL.setCutoff(200.0, sampleRate)
        lpR.setCutoff(200.0, sampleRate)
        return r
    }

    override fun process(samples: FloatArray) {
        if (channels < 2) return
        val mix = amount.coerceIn(0f, 1f)
        val gain = dbToLinear(bassGainDb)
        var i = 0
        while (i + 1 < samples.size) {
            val l = samples[i]
            val r = samples[i + 1]

            val lowL = lpL.process(l)
            val lowR = lpR.process(r)

            // Envelope follower on bass
            val absL = abs(lowL); val absR = abs(lowR)
            envL = if (absL > envL) absL else envL * 0.9995f
            envR = if (absR > envR) absR else envR * 0.9995f

            // Compress the bass: reduce when too hot
            val compL = 1f / (1f + envL * 2f)
            val compR = 1f / (1f + envR * 2f)

            val boostL = lowL * (gain - 1f) * compL * mix
            val boostR = lowR * (gain - 1f) * compR * mix

            samples[i]     = l + boostL
            samples[i + 1] = r + boostR
            i += 2
        }
    }

    override fun onReset() {
        lpL.reset(); lpR.reset()
        envL = 0f; envR = 0f
    }
}

// ═══════════════════════════════════════════════════════
// Exciter — harmonic generator (presence/air)
// ═══════════════════════════════════════════════════════
@UnstableApi
class ExciterProcessor(
    @Volatile var amount: Float = 0.2f,   // 0..1
    @Volatile var cutoffHz: Float = 3000f
) : FloatProcessor() {

    private val hpL = OnePole()
    private val hpR = OnePole()

    override fun configure(input: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val r = super.configure(input)
        hpL.setCutoff(cutoffHz.toDouble(), sampleRate)
        hpR.setCutoff(cutoffHz.toDouble(), sampleRate)
        return r
    }

    override fun process(samples: FloatArray) {
        if (channels < 2) return
        val a = amount.coerceIn(0f, 1f)
        var i = 0
        while (i + 1 < samples.size) {
            val l = samples[i]
            val r = samples[i + 1]

            val hp_l = l - hpL.process(l)
            val hp_r = r - hpR.process(r)

            // Soft non-linearity → harmonics
            val harmL = tanh(hp_l * 2f) - hp_l
            val harmR = tanh(hp_r * 2f) - hp_r

            samples[i]     = l + harmL * a * 0.5f
            samples[i + 1] = r + harmR * a * 0.5f
            i += 2
        }
    }

    override fun onReset() { hpL.reset(); hpR.reset() }
}

// ═══════════════════════════════════════════════════════
// Soft Limiter — smooth ceiling
// ═══════════════════════════════════════════════════════
@UnstableApi
class SoftLimiterProcessor(
    @Volatile var ceiling: Float = 0.95f,
    @Volatile var enabled: Boolean = true
) : FloatProcessor() {

    override fun process(samples: FloatArray) {
        if (!enabled) return
        val c = ceiling.coerceIn(0.5f, 1.0f)
        for (i in samples.indices) {
            val x = samples[i]
            if (x > c || x < -c) {
                // Smooth soft-clip
                val s = if (x > 0) 1f else -1f
                val ax = abs(x) / c
                samples[i] = s * c * (1f - exp(-ax))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════
private fun dbToLinear(db: Float): Float =
    Math.pow(10.0, db / 20.0).toFloat()

private class OnePole {
    private var a = 0.1f
    private var y = 0f

    fun setCutoff(hz: Double, sampleRate: Int) {
        val dt = 1.0 / sampleRate
        val rc = 1.0 / (2.0 * Math.PI * hz)
        a = (dt / (rc + dt)).toFloat()
    }

    fun process(x: Float): Float {
        y += a * (x - y)
        return y
    }

    fun reset() { y = 0f }
}
