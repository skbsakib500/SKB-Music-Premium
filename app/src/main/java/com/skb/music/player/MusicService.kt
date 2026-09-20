package com.skb.music.player

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        PlayerHolder.player = player

        // ═══ Full DSP chain ═══
        runCatching {
            AudioProfileManager.attach(player)
            AudioProfileManager.applyGenre("Flat")
            AudioProfileManager.setBassStrength(0.5f)
            AudioProfileManager.setVirtualStrength(0.3f)
            AudioProfileManager.setLoudnessGain(0)
        }

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        runCatching { AudioProfileManager.release() }
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        PlayerHolder.player = null
        super.onDestroy()
    }
}
