package com.skb.music.player

import android.content.Intent
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.skb.music.player.dsp.CrossfeedProcessor
import com.skb.music.player.dsp.DynamicBassProcessor
import com.skb.music.player.dsp.ExciterProcessor
import com.skb.music.player.dsp.PreampProcessor
import com.skb.music.player.dsp.SoftLimiterProcessor
import com.skb.music.player.dsp.StereoWidthProcessor

@UnstableApi
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val processors = mutableListOf<AudioProcessor>()

        // 1) Hi-Res upsample
        if (HiResSettings.enabled.value && HiResSettings.effectiveFactor() > 1) {
            processors += HiResUpsampler(HiResSettings.effectiveFactor())
        }

        // 2) Advanced DSP
        if (AudioPipeline.dspEnabled.value) {
            processors += PreampProcessor(
                gainDb = AudioPipeline.preampDb.value,
                headroomDb = AudioPipeline.headroomDb.value
            )
            processors += StereoWidthProcessor(AudioPipeline.stereoWidth.value)
            processors += CrossfeedProcessor(AudioPipeline.crossfeed.value)
            processors += DynamicBassProcessor(
                amount = AudioPipeline.dynBass.value,
                bassGainDb = AudioPipeline.dynBassGain.value
            )
            processors += ExciterProcessor(AudioPipeline.exciter.value)
            processors += SoftLimiterProcessor(
                ceiling = AudioPipeline.limiterCeil.value,
                enabled = AudioPipeline.limiterOn.value
            )
        }

        val sink = DefaultAudioSink.Builder(this)
            .setAudioProcessors(processors.toTypedArray())
            .setEnableFloatOutput(true)
            .setAudioCapabilities(AudioCapabilities.getCapabilities(this))
            .build()

        val p = ExoPlayer.Builder(this).setAudioSink(sink).build()
        player = p
        PlayerHolder.player = p

        // AudioEffect DSP (session-based)
        runCatching {
            AudioProfileManager.attach(p)
            AudioProfileManager.applyGenre("Flat")
            AudioProfileManager.setBassStrength(0.5f)
            AudioProfileManager.setVirtualStrength(0.3f)
        }

        mediaSession = MediaSession.Builder(this, p).build()
    }

    override fun onGetSession(c: MediaSession.ControllerInfo) = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        runCatching { AudioProfileManager.release() }
        player?.release()
        mediaSession?.release()
        mediaSession = null
        player = null
        PlayerHolder.player = null
        super.onDestroy()
    }
}
