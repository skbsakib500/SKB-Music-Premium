package com.skb.music.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A high-quality 32-bit float upsampler for Media3 ExoPlayer.
 *
 * Catmull-Rom cubic interpolation (4-point). Better than linear
 * for music. Output is always ENCODING_PCM_FLOAT (32-bit).
 *
 * Usage:
 *   DefaultAudioSink.Builder(context)
 *       .setAudioProcessors(arrayOf(HiResUpsampler(4)))
 *       .setEnableFloatOutput(true)
 *       .build()
 */
@UnstableApi
class HiResUpsampler(
    private var factor: Int = 4
) : AudioProcessor {

    private var channels = 2
    private var inRate = 48_000
    private var inEncoding = C.ENCODING_PCM_16BIT
    private var active = false

    private var output = AudioProcessor.EMPTY_BUFFER
    private var endOfStream = false

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (factor < 1) factor = 1
        channels = inputAudioFormat.channelCount
        inRate = inputAudioFormat.sampleRate
        inEncoding = inputAudioFormat.encoding

        val outRate = (inRate * factor).coerceAtMost(768_000)
        // Always output 32-bit float
        active = true
        return AudioProcessor.AudioFormat(
            C.ENCODING_PCM_FLOAT,
            channels,
            outRate
        )
    }

    override fun isActive(): Boolean = active

    override fun queueInput(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.nativeOrder())
        if (!inputBuffer.hasRemaining()) {
            output = AudioProcessor.EMPTY_BUFFER
            return
        }

        val samples = readAsFloat(inputBuffer)
        val upsampled = if (factor > 1) cubicUpsample(samples, factor) else samples

        val out = ByteBuffer.allocate(upsampled.size * 4).order(ByteOrder.nativeOrder())
        for (s in upsampled) out.putFloat(s)
        out.flip()
        output = out
    }

    override fun onQueueEndOfStream() {
        endOfStream = true
    }

    override fun getOutput(): ByteBuffer {
        val o = output
        output = AudioProcessor.EMPTY_BUFFER
        return o
    }

    override fun hasPendingOutput(): Boolean = output.hasRemaining()

    override fun flush() {
        output = AudioProcessor.EMPTY_BUFFER
        endOfStream = false
    }

    override fun reset() {
        flush()
        active = false
    }

    // ─────────────────────────────────────────────────
    // ByteBuffer → Float array (handles 16/24/32-bit PCM + float)
    // ─────────────────────────────────────────────────
    private fun readAsFloat(buf: ByteBuffer): FloatArray {
        val bytesPerSample = when (inEncoding) {
            C.ENCODING_PCM_FLOAT -> 4
            C.ENCODING_PCM_16BIT -> 2
            C.ENCODING_PCM_24BIT -> 3
            C.ENCODING_PCM_32BIT -> 4
            else -> 2
        }
        val n = buf.remaining() / bytesPerSample
        val out = FloatArray(n)

        for (i in 0 until n) {
            out[i] = when (inEncoding) {
                C.ENCODING_PCM_FLOAT -> buf.float
                C.ENCODING_PCM_16BIT -> buf.short / 32768f
                C.ENCODING_PCM_24BIT -> {
                    val b0 = buf.get().toInt() and 0xFF
                    val b1 = buf.get().toInt() and 0xFF
                    val b2 = buf.get().toInt()
                    var v = (b2 shl 16) or (b1 shl 8) or b0
                    if (v and 0x800000 != 0) v = v or -0x1000000
                    v / 8388608f
                }
                C.ENCODING_PCM_32BIT -> buf.int / 2147483648f
                else -> 0f
            }
        }
        return out
    }

    // ─────────────────────────────────────────────────
    // Catmull-Rom cubic interpolation (4-point)
    // ─────────────────────────────────────────────────
    private fun cubicUpsample(src: FloatArray, factor: Int): FloatArray {
        if (src.size < 4) return linearUpsample(src, factor)
        val out = FloatArray(src.size * factor)
        val last = src.size - 1

        for (i in src.indices) {
            val p0 = src[(i - 1).coerceAtLeast(0)]
            val p1 = src[i]
            val p2 = src[(i + 1).coerceAtMost(last)]
            val p3 = src[(i + 2).coerceAtMost(last)]

            for (k in 0 until factor) {
                val t = k.toFloat() / factor
                val t2 = t * t
                val t3 = t2 * t

                // Catmull-Rom coefficients
                val a = -0.5f * p0 + 1.5f * p1 - 1.5f * p2 + 0.5f * p3
                val b = p0 - 2.5f * p1 + 2f * p2 - 0.5f * p3
                val c = -0.5f * p0 + 0.5f * p2
                val d = p1

                val v = a * t3 + b * t2 + c * t + d
                // Soft clip against overshoot
                out[i * factor + k] = v.coerceIn(-1f, 1f)
            }
        }
        return out
    }

    private fun linearUpsample(src: FloatArray, factor: Int): FloatArray {
        if (src.isEmpty()) return src
        val out = FloatArray(src.size * factor)
        for (i in src.indices) {
            val cur = src[i]
            val nxt = if (i + 1 < src.size) src[i + 1] else cur
            for (k in 0 until factor) {
                val t = k.toFloat() / factor
                out[i * factor + k] = cur + (nxt - cur) * t
            }
        }
        return out
    }
}
