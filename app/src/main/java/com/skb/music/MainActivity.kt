package com.skb.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.skb.music.data.MusicRepository
import com.skb.music.data.Song
import com.skb.music.player.MusicService
import com.skb.music.player.PlayerHolder
import com.skb.music.ui.components.GradientBackground
import com.skb.music.ui.screens.HomeScreen
import com.skb.music.ui.screens.PlayerScreen
import com.skb.music.ui.screens.SettingsScreen
import com.skb.music.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = MusicRepository(this)

        // Start foreground service so ExoPlayer lives
        startService(Intent(this, MusicService::class.java))

        setContent {
            SKBMusicTheme {
                var hasPerm by remember { mutableStateOf(checkAudioPermission()) }
                val permLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { hasPerm = it }

                LaunchedEffect(Unit) {
                    if (!hasPerm) permLauncher.launch(audioPermission())
                }

                if (!hasPerm) {
                    PermissionScreen { permLauncher.launch(audioPermission()) }
                } else {
                    AppRoot(repo)
                }
            }
        }
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private fun checkAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, audioPermission()) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    GradientBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "Audio Access Needed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AmuletText
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Grant access to play music from your device.",
                    color = AmuletTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmuletEmerald,
                        contentColor = Color.Black
                    )
                ) { Text("Grant Permission") }
            }
        }
    }
}

@Composable
private fun AppRoot(repo: MusicRepository) {
    var tab by remember { mutableIntStateOf(0) }
    var currentSong by remember { mutableStateOf<Song?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    // Poll player state
    LaunchedEffect(Unit) {
        while (true) {
            PlayerHolder.player?.let { p ->
                isPlaying = p.isPlaying
                position = p.currentPosition.coerceAtLeast(0L)
                duration = p.duration.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = AmuletSurface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmuletEmerald,
                            selectedTextColor = AmuletEmerald,
                            indicatorColor = AmuletEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = AmuletTextMuted,
                            unselectedTextColor = AmuletTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.LibraryMusic, null) },
                        label = { Text("Player") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmuletEmerald,
                            selectedTextColor = AmuletEmerald,
                            indicatorColor = AmuletEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = AmuletTextMuted,
                            unselectedTextColor = AmuletTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmuletEmerald,
                            selectedTextColor = AmuletEmerald,
                            indicatorColor = AmuletEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = AmuletTextMuted,
                            unselectedTextColor = AmuletTextMuted
                        )
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    0 -> HomeScreen(repo) { song, list ->
                        currentSong = song
                        val idx = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                        PlayerHolder.setQueue(list, idx)
                        tab = 1
                    }
                    1 -> PlayerScreen(
                        song = currentSong,
                        isPlaying = isPlaying,
                        positionMs = position,
                        durationMs = duration,
                        onPlayPause = {
                            PlayerHolder.player?.let { p ->
                                if (p.isPlaying) p.pause() else p.play()
                            }
                        },
                        onNext = { PlayerHolder.player?.seekToNextMediaItem() },
                        onPrevious = { PlayerHolder.player?.seekToPreviousMediaItem() },
                        onSeek = { PlayerHolder.player?.seekTo(it) }
                    )
                    2 -> SettingsScreen()
                }
            }
        }
    }
}
