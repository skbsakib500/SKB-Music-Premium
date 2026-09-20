package com.skb.music.player

import android.content.Intent
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@UnstableApi
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        // ═══ Build 32-bit float pipeline with custom upsampler ═══
        val processors: Array<AudioProcessor> =
            if (HiResSettings.enabled.value && HiResSettings.effectiveFactor() > 1) {
                arrayOf(HiResUpsampler(HiResSettings.effectiveFactor()))
            } else emptyArray()

        val sink = DefaultAudioSink.Builder(this)
            .setAudioProcessors(processors)
            .setEnableFloatOutput(HiResSettings.float32.value)
            .setAudioCapabilities(AudioCapabilities.getCapabilities(this))
            .build()

        val p = ExoPlayer.Builder(this)
            .setAudioSink(sink)
            .build()

        player = p
        PlayerHolder.player = p

        // ═══ Full DSP chain ═══
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
