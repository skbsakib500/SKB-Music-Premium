package com.skb.music.data

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        runCatching {
            context.contentResolver.query(
                collection, projection, selection, null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val albumId = c.getLong(albumIdCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val artUri = Uri.parse("content://media/external/audio/albumart/$albumId")
                    val path = c.getString(dataCol) ?: ""

                    songs.add(
                        Song(
                            id = id,
                            title = c.getString(titleCol) ?: "Unknown",
                            artist = c.getString(artistCol) ?: "Unknown Artist",
                            album = c.getString(albumCol) ?: "Unknown Album",
                            duration = c.getLong(durCol),
                            uri = uri,
                            albumArtUri = artUri,
                            path = path
                        )
                    )
                }
            }
        }
        songs
    }

    /**
     * Android 11+ এ delete request URI রিটার্ন করে (user confirmation)
     * Android 10 বা তার নিচে সরাসরি delete হয়, null রিটার্ন করে
     */
    fun buildDeleteRequest(song: Song): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                MediaStore.createDeleteRequest(context.contentResolver, listOf(song.uri))
            }.getOrNull()
        } else null
    }

    /**
     * সরাসরি delete (Android 10 বা তার নিচে)
     */
    fun deleteDirect(song: Song): Boolean {
        return runCatching {
            context.contentResolver.delete(song.uri, null, null) > 0
        }.getOrDefault(false)
    }

    /**
     * Android 10 এ RecoverableSecurityException হলে এটা throw করে
     */
    fun tryDelete(song: Song) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            runCatching {
                context.contentResolver.delete(song.uri, null, null)
            }.onFailure { t ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    t is RecoverableSecurityException
                ) throw t
            }
        }
    }
}
