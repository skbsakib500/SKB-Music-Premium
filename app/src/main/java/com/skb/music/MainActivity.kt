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
import androidx.compose.material.icons.filled.*
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
import com.skb.music.ui.screens.*
import com.skb.music.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = MusicRepository(this)
        startService(Intent(this, MusicService::class.java))

        setContent {
            SKBMusicTheme {
                var hasPerm by remember { mutableStateOf(checkAudioPermission()) }
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { hasPerm = it }
                LaunchedEffect(Unit) {
                    if (!hasPerm) launcher.launch(audioPermission())
                }
                if (!hasPerm) PermissionScreen { launcher.launch(audioPermission()) }
                else AppRoot(repo)
            }
        }
    }
    private fun audioPermission() =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    private fun checkAudioPermission() =
        ContextCompat.checkSelfPermission(this, audioPermission()) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    GradientBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)) {
                Text("Audio Access Needed", style = MaterialTheme.typography.headlineMedium, color = AmuletText)
                Spacer(Modifier.height(12.dp))
                Text("Grant access to play music.", color = AmuletTextMuted)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = AmuletEmerald, contentColor = Color.Black)
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
                NavigationBar(containerColor = AmuletSurface, tonalElevation = 0.dp) {
                    val items = listOf(
                        Triple("Home", Icons.Default.Home, 0),
                        Triple("Player", Icons.Default.PlayArrow, 1),
                        Triple("EQ", Icons.Default.Tune, 2),
                        Triple("Vis", Icons.Default.GraphicEq, 3),
                        Triple("More", Icons.Default.Settings, 4)
                    )
                    items.forEach { (label, icon, idx) ->
                        NavigationBarItem(
                            selected = tab == idx,
                            onClick = { tab = idx },
                            icon = { Icon(icon, null) },
                            label = { Text(label) },
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
                        song = currentSong, isPlaying = isPlaying,
                        positionMs = position, durationMs = duration,
                        onPlayPause = {
                            PlayerHolder.player?.let { p ->
                                if (p.isPlaying) p.pause() else p.play()
                            }
                        },
                        onNext = { PlayerHolder.player?.seekToNextMediaItem() },
                        onPrevious = { PlayerHolder.player?.seekToPreviousMediaItem() },
                        onSeek = { PlayerHolder.player?.seekTo(it) }
                    )
                    2 -> EqualizerScreen()
                    3 -> VisualizerScreen()
                    4 -> SettingsScreen()
                }
            }
        }
    }
}
