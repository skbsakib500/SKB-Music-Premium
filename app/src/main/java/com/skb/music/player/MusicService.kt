package com.skb.music.player

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        PlayerHolder.player = player
        // সংযুক্ত ইকুয়ালাইজার
        EqualizerManager.attach(player)
        EqualizerManager.applyPreset("Flat")

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        EqualizerManager.release()
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        PlayerHolder.player = null
        super.onDestroy()
    }
}
