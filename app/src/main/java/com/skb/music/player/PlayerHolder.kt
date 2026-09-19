package com.skb.music.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.skb.music.data.Song

object PlayerHolder {
    var player: ExoPlayer? = null

    fun setQueue(songs: List<Song>, startIndex: Int) {
        val p = player ?: return
        val items = songs.map {
            MediaItem.Builder()
                .setUri(it.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.artist)
                        .setAlbumTitle(it.album)
                        .setArtworkUri(it.albumArtUri)
                        .build()
                )
                .build()
        }
        p.setMediaItems(items, startIndex, 0)
        p.prepare()
        p.play()
    }
}
