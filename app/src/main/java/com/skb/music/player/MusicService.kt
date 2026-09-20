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
import com.skb.music.player.spatial.Binaural3DProcessor
import com.skb.music.player.spatial.Hyper10DProcessor
import com.skb.music.player.spatial.Rotate8DProcessor
import com.skb.music.player.spatial.SpatialMode
import com.skb.music.player.spatial.SpatialType

@UnstableApi
class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        // Build audio processors
        val processors = mutableListOf<AudioProcessor>()

        // Hi-Res upsampler
        if (HiResSettings.enabled.value && HiResSettings.effectiveFactor() > 1) {
            processors += HiResUpsampler(HiResSettings.effectiveFactor())
        }

        // DSP chain
        if (AudioPipeline.dspEnabled.value) {
            processors += PreampProcessor(
                AudioPipeline.preampDb.value,
                AudioPipeline.headroomDb.value
            )
            processors += StereoWidthProcessor(AudioPipeline.stereoWidth.value)
            processors += CrossfeedProcessor(AudioPipeline.crossfeed.value)
            processors += DynamicBassProcessor(
                AudioPipeline.dynBass.value,
                AudioPipeline.dynBassGain.value
            )
            processors += ExciterProcessor(AudioPipeline.exciter.value)
        }

        // Spatial
        if (SpatialMode.enabled.value) {
            when (SpatialMode.type.value) {
                SpatialType.SPATIAL_8D -> processors += Rotate8DProcessor(
                    SpatialMode.rotateSpeed.value,
                    SpatialMode.rotateWet.value,
                    SpatialMode.rotateRadius.value
                )
                SpatialType.SPATIAL_10D -> processors += Hyper10DProcessor(
                    SpatialMode.hyperSpeed.value,
                    SpatialMode.hyperWet.value,
                    SpatialMode.hyperDepth.value,
                    SpatialMode.hyperHaas.value
                )
                SpatialType.BINAURAL_3D -> processors += Binaural3DProcessor(
                    SpatialMode.binauralWidth.value,
                    SpatialMode.binauralDepth.value,
                    SpatialMode.binauralElev.value
                )
                SpatialType.OFF -> {}
            }
        }

        // Limiter
        if (AudioPipeline.dspEnabled.value && AudioPipeline.limiterOn.value) {
            processors += SoftLimiterProcessor(
                ceiling = AudioPipeline.limiterCeil.value,
                enabled = true
            )
        }

        // ═══ Build DefaultAudioSink with processors ═══
        val sinkBuilder = DefaultAudioSink.Builder(this)
            .setAudioCapabilities(AudioCapabilities.getCapabilities(this))
            .setEnableFloatOutput(true)

        if (processors.isNotEmpty()) {
            sinkBuilder.setAudioProcessors(processors.toTypedArray())
        }

        val sink = sinkBuilder.build()

        // ═══ Create ExoPlayer with custom sink ═══
        val p = ExoPlayer.Builder(this)
            .setAudioSink(sink)
            .build()

        player = p
        PlayerHolder.player = p

        // Session-based AudioEffect chain
        runCatching {
            AudioProfileManager.attach(p)
            AudioProfileManager.applyGenre("Flat")
            AudioProfileManager.setBassStrength(0.5f)
            AudioProfileManager.setVirtualStrength(0.3f)
        }

        mediaSession = MediaSession.Builder(this, p).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

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
