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

        runCatching {
            EqualizerManager.attach(player)
            EqualizerManager.applyPreset("Flat")
        }

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        runCatching { EqualizerManager.release() }
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        PlayerHolder.player = null
        super.onDestroy()
    }
}
