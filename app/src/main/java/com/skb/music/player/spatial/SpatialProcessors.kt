package com.skb.music.player.spatial

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

// ═══════════════════════════════════════════════════════
// Base — 32-bit float, stereo only
// ═══════════════════════════════════════════════════════
@UnstableApi
abstract class SpatialBase : AudioProcessor {
    protected var channels = 2
    protected var sampleRate = 48000
    protected var active = false
    private var out: ByteBuffer = AudioProcessor.EMPTY_BUFFER

    override fun configure(input: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        channels = input.channelCount
        sampleRate = input.sampleRate
        active = channels >= 2
        return AudioProcessor.AudioFormat(
            C.ENCODING_PCM_FLOAT,
            input.channelCount,
            input.sampleRate
        )
    }

    override fun isActive(): Boolean = active
    override fun isEnded(): Boolean = false
    override fun isEnded(): Boolean = false

    override fun queueInput(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.nativeOrder())
        if (!inputBuffer.hasRemaining()) {
            out = AudioProcessor.EMPTY_BUFFER; return
        }
        val n = inputBuffer.remaining() / 4
        val s = FloatArray(n)
        inputBuffer.asFloatBuffer().get(s)
        inputBuffer.position(inputBuffer.limit())
        if (active) process(s)
        val o = ByteBuffer.allocate(s.size * 4).order(ByteOrder.nativeOrder())
        o.asFloatBuffer().put(s); o.flip()
        out = o
    }

    override fun queueEndOfStream() {}
    override fun getOutput(): ByteBuffer {
        val o = out; out = AudioProcessor.EMPTY_BUFFER; return o
    }
    override fun flush() { out = AudioProcessor.EMPTY_BUFFER }
    override fun reset() { flush(); active = false; onReset() }

    protected abstract fun process(s: FloatArray)
    protected open fun onReset() {}
}

// ═══════════════════════════════════════════════════════
// 8D Processor — rotating pan + reverb tail
// ═══════════════════════════════════════════════════════
@UnstableApi
class Rotate8DProcessor(
    @Volatile var speedHz: Float = 0.08f,     // rotation speed (Hz)
    @Volatile var wetMix: Float = 0.25f,      // reverb mix
    @Volatile var radius: Float = 1.0f        // 0..1 (circular)
) : SpatialBase() {

    private var phase = 0f
    private var lpL = 0f
    private var lpR = 0f
    private val revL = CombBank(sampleRate)
    private val revR = CombBank(sampleRate)

    override fun process(s: FloatArray) {
        val twoPi = 2f * PI.toFloat()
        val step = twoPi * speedHz / sampleRate
        val mix = wetMix.coerceIn(0f, 0.6f)
        val rad = radius.coerceIn(0f, 1f)

        var i = 0
        while (i + 1 < s.size) {
            val l = s[i]
            val r = s[i + 1]
            val mono = (l + r) * 0.5f

            // Rotate source around listener
            val angle = phase
            val gainL = cos(angle * 0.5f).coerceIn(0.2f, 1f)
            val gainR = sin(angle * 0.5f).coerceIn(0.2f, 1f)

            // Wet = mono source panned around circle + reverb
            val wetL0 = mono * gainL * rad
            val wetR0 = mono * gainR * rad

            // Simple low-pass on wet for warmth
            lpL += 0.15f * (wetL0 - lpL)
            lpR += 0.15f * (wetR0 - lpR)

            // Reverb on wet
            val rvL = revL.process(lpL)
            val rvR = revR.process(lpR)

            s[i]     = l * (1f - mix) + (lpL + rvL * 0.35f) * mix
            s[i + 1] = r * (1f - mix) + (lpR + rvR * 0.35f) * mix

            phase += step
            if (phase > twoPi) phase -= twoPi
            i += 2
        }
    }

    override fun onReset() {
        phase = 0f; lpL = 0f; lpR = 0f
        revL.reset(); revR.reset()
    }
}

// ═══════════════════════════════════════════════════════
// 10D Processor — 8D + Haas + HRTF-lite + room
// ═══════════════════════════════════════════════════════
@UnstableApi
class Hyper10DProcessor(
    @Volatile var speedHz: Float = 0.05f,
    @Volatile var wetMix: Float = 0.35f,
    @Volatile var depth: Float = 0.8f,       // 0..1 binaural depth
    @Volatile var haasMs: Float = 12f        // 0..30
) : SpatialBase() {

    private var phase = 0f
    private val delayL = DelayLine(sampleRate, 0.05f)
    private val delayR = DelayLine(sampleRate, 0.05f)
    private val revL = CombBank(sampleRate)
    private val revR = CombBank(sampleRate)

    // HRTF approximation: frequency-dependent gain + delay
    private var shadowL = 0f
    private var shadowR = 0f

    override fun process(s: FloatArray) {
        val twoPi = 2f * PI.toFloat()
        val step = twoPi * speedHz / sampleRate
        val haasSamples = ((haasMs / 1000f) * sampleRate).toInt()
        val mix = wetMix.coerceIn(0f, 0.6f)
        val d = depth.coerceIn(0f, 1f)

        var i = 0
        while (i + 1 < s.size) {
            val l = s[i]
            val r = s[i + 1]
            val mono = (l + r) * 0.5f

            // Continuous rotation
            val angle = phase
            val panL = cos(angle).coerceIn(-1f, 1f)
            val panR = sin(angle).coerceIn(-1f, 1f)

            // HRTF-lite: head-shadow filter (low-pass on far side)
            // When source is to the left, right ear hears less highs
            val shadow = 0.6f + 0.4f * abs(cos(angle))
            val shadowOther = 0.6f + 0.4f * abs(sin(angle))

            // Delay for ITD (inter-aural time difference)
            val delaySamples = (abs(sin(angle)) * 0.0006f * sampleRate).toInt() // up to 0.6ms
            delayL.write(mono); delayR.write(mono)
            val dL = delayL.read(delaySamples)
            val dR = delayR.read(delaySamples)

            // Haas: subtle short delay on one side
            val haasL = delayL.read(haasSamples)
            val haasR = delayR.read(haasSamples)

            // Left ear
            val wetL = (dL * shadow + haasL * 0.3f) * (0.5f + panL * 0.5f) * d
            // Right ear
            val wetR = (dR * shadowOther + haasR * 0.3f) * (0.5f + panR * 0.5f) * d

            // Room reverb
            val rvL = revL.process(wetL)
            val rvR = revR.process(wetR)

            // Cross-feed binaural mix
            val outL = wetL + rvL * 0.4f + wetR * 0.15f
            val outR = wetR + rvR * 0.4f + wetL * 0.15f

            s[i]     = l * (1f - mix) + outL * mix
            s[i + 1] = r * (1f - mix) + outR * mix

            phase += step
            if (phase > twoPi) phase -= twoPi
            i += 2
        }
    }

    override fun onReset() {
        phase = 0f
        delayL.reset(); delayR.reset()
        revL.reset(); revR.reset()
        shadowL = 0f; shadowR = 0f
    }
}

// ═══════════════════════════════════════════════════════
// 3D Binaural — HRTF-style fixed-position virtualization
// ═══════════════════════════════════════════════════════
@UnstableApi
class Binaural3DProcessor(
    @Volatile var width: Float = 0.7f,        // 0..1
    @Volatile var depth: Float = 0.5f,        // 0..1
    @Volatile var elevation: Float = 0.0f     // -1..1
) : SpatialBase() {

    private val delayL = DelayLine(sampleRate, 0.03f)
    private val delayR = DelayLine(sampleRate, 0.03f)
    private val revL = CombBank(sampleRate)
    private val revR = CombBank(sampleRate)

    override fun process(s: FloatArray) {
        val itdSamples = (0.0005f * sampleRate).toInt() // 0.5ms
        val w = width.coerceIn(0f, 1f)
        val d = depth.coerceIn(0f, 1f)
        val e = elevation.coerceIn(-1f, 1f)

        var i = 0
        while (i + 1 < s.size) {
            val l = s[i]
            val r = s[i + 1]

            delayL.write(l); delayR.write(r)

            // ITD: right ear hears left channel delayed and vice versa
            val delayedR_byL = delayR.read(itdSamples)
            val delayedL_byR = delayL.read(itdSamples)

            // ILD: far ear attenuated
            val farGain = 0.6f + 0.4f * (1f - w)

            // Binaural reconstruction
            val outL = l + delayedL_byR * farGain * w
            val outR = r + delayedR_byL * farGain * w

            // Depth via reverb
            val rvL = revL.process(outL) * d
            val rvR = revR.process(outR) * d

            // Elevation: gentle shelf (more highs above, less below)
            val elev = 1f + e * 0.15f

            s[i]     = (outL + rvL) * elev
            s[i + 1] = (outR + rvR) * elev
            i += 2
        }
    }

    override fun onReset() {
        delayL.reset(); delayR.reset(); revL.reset(); revR.reset()
    }
}

// ═══════════════════════════════════════════════════════
// Helpers — delay + comb/reverb
// ═══════════════════════════════════════════════════════
private class DelayLine(sampleRate: Int, maxSeconds: Float) {
    private val buf = FloatArray((sampleRate * maxSeconds).toInt().coerceAtLeast(64))
    private var pos = 0

    fun write(v: Float) {
        buf[pos] = v
        pos = (pos + 1) % buf.size
    }

    fun read(delaySamples: Int): Float {
        val d = delaySamples.coerceIn(0, buf.size - 1)
        var idx = pos - d
        if (idx < 0) idx += buf.size
        return buf[idx]
    }

    fun reset() { buf.fill(0f); pos = 0 }
}

private class CombBank(sampleRate: Int) {
    private val c1 = Comb(1557); private val c2 = Comb(1617)
    private val c3 = Comb(1491); private val c4 = Comb(1422)
    private var allpass = AllPass(225)

    fun process(x: Float): Float {
        val out = c1.process(x) + c2.process(x) + c3.process(x) + c4.process(x)
        return allpass.process(out * 0.25f)
    }
    fun reset() { c1.reset(); c2.reset(); c3.reset(); c4.reset(); allpass.reset() }
}

private class Comb(size: Int) {
    private val buf = FloatArray(size)
    private var pos = 0
    private val feedback = 0.78f
    private val damp = 0.3f
    private var last = 0f

    fun process(x: Float): Float {
        val y = buf[pos]
        last = y * (1f - damp) + last * damp
        buf[pos] = x + last * feedback
        pos = (pos + 1) % buf.size
        return y
    }
    fun reset() { buf.fill(0f); pos = 0; last = 0f }
}

private class AllPass(size: Int) {
    private val buf = FloatArray(size)
    private var pos = 0
    private val feedback = 0.5f
    fun process(x: Float): Float {
        val y = buf[pos]
        buf[pos] = x + y * feedback
        pos = (pos + 1) % buf.size
        return y - x * feedback
    }
    fun reset() { buf.fill(0f); pos = 0 }
}
