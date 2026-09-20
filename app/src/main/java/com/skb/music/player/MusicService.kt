package com.skb.music.player

import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@UnstableApi
class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val p = ExoPlayer.Builder(this).build()
        player = p
        PlayerHolder.player = p

        // AudioEffect chain — All Android-native
        runCatching {
            AudioProfileManager.attach(p)
            AudioProfileManager.applyGenre("Flat")
            AudioProfileManager.setBassStrength(0.5f)
            AudioProfileManager.setVirtualStrength(0.3f)
            AudioProfileManager.setLoudnessGain(0)
        }

        // Spatial — 8D / 10D / 3D
        runCatching {
            SpatialAudioManager.attach(p)
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
        runCatching { SpatialAudioManager.release() }
        player?.release()
        mediaSession?.release()
        mediaSession = null
        player = null
        PlayerHolder.player = null
        super.onDestroy()
    }
}
