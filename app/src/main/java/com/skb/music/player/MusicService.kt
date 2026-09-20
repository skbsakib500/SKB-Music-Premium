package com.skb.music.player

import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.Player
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

        // ✅ Fix "music stops after a while"
        p.setWakeMode(C.WAKE_MODE_NETWORK)
        p.setHandleAudioBecomingNoisy(true)
        p.setHandleAudioFocus(true)

        // ✅ Attach AudioEffects the moment session ID is ready
        p.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    AudioProfileManager.attach(audioSessionId)
                    SpatialAudioManager.apply(SpatialAudioManager.currentMode)
                }
            }
        })

        PlayerHolder.player = p
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
