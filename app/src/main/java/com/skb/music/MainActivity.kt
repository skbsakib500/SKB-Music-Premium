package com.skb.music

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.skb.music.data.MusicRepository
import com.skb.music.data.Song
import com.skb.music.player.MusicService
import com.skb.music.player.PlaybackMode
import com.skb.music.player.PlayerHolder
import com.skb.music.ui.components.GradientBackground
import com.skb.music.ui.screens.EqualizerScreen
import com.skb.music.ui.screens.HomeScreen
import com.skb.music.ui.screens.LibraryScreen
import com.skb.music.ui.screens.PlayerScreen
import com.skb.music.ui.screens.QueueScreen
import com.skb.music.ui.screens.SearchScreen
import com.skb.music.ui.screens.SettingsScreen
import com.skb.music.ui.screens.SplashScreen
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletSurface
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted
import com.skb.music.ui.theme.SKBMusicTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = MusicRepository(this)
        startService(Intent(this, MusicService::class.java))
        setContent {
            SKBMusicTheme {
                var splashDone by remember { mutableStateOf(false) }
                var hasPerm by remember { mutableStateOf(checkPerm()) }
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { hasPerm = it }

                LaunchedEffect(splashDone) {
                    if (splashDone && !hasPerm) launcher.launch(audioPerm())
                }

                when {
                    !splashDone -> SplashScreen { splashDone = true }
                    !hasPerm -> PermissionScreen { launcher.launch(audioPerm()) }
                    else -> AppRoot(repo)
                }
            }
        }
    }
    private fun audioPerm() =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    private fun checkPerm() =
        ContextCompat.checkSelfPermission(this, audioPerm()) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    // Back button navigation
    BackHandler(enabled = screen != AppScreen.TABS) {
        screen = when (screen) {
            AppScreen.QUEUE -> AppScreen.PLAYER
            else -> AppScreen.TABS
        }
    }

    GradientBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "Audio Access Needed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AmuletText
                )
                Spacer(Modifier.height(12.dp))
                Text("Grant access to play music.", color = AmuletTextMuted)
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

enum class AppScreen { TABS, PLAYER, QUEUE }

@Composable
private fun AppRoot(repo: MusicRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var tab by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf(AppScreen.TABS) }
    var libraryScrollIndex by remember { mutableIntStateOf(0) }
    var libraryScrollOffset by remember { mutableIntStateOf(0) }

    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var currentSong by remember { mutableStateOf<Song?>(null) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var favorites by remember { mutableStateOf<Set<Long>>(emptySet()) }

    suspend fun reload() {
        allSongs = repo.loadSongs()
    }

    LaunchedEffect(Unit) { reload() }

    LaunchedEffect(Unit) {
        while (true) {
            PlayerHolder.player?.let { p ->
                isPlaying = p.isPlaying
                position = p.currentPosition.coerceAtLeast(0L)
                duration = p.duration.coerceAtLeast(0L)
                currentIndex = p.currentMediaItemIndex
                if (currentIndex in allSongs.indices)
                    currentSong = allSongs[currentIndex]
                PlaybackMode.syncFrom(p)
            }
            delay(500)
        }
    }

    fun playSong(song: Song, list: List<Song>) {
        currentSong = song
        val idx = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        PlayerHolder.setQueue(list, idx)
        screen = AppScreen.PLAYER
    }

    // ═══ DELETE Flow ═══
    var pendingDelete by remember { mutableStateOf<Song?>(null) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch { reload() }
        }
        pendingDelete = null
    }

    fun requestDelete(song: Song) {
        val pending = repo.buildDeleteRequest(song)
        if (pending != null) {
            runCatching {
                val sender = pending.intentSender
                deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
            }.onFailure {
                // Fallback direct delete
                repo.deleteDirect(song)
                scope.launch { reload() }
            }
        } else {
            repo.deleteDirect(song)
            scope.launch { reload() }
        }
    }

    GradientBackground {
        when (screen) {

            AppScreen.PLAYER -> Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { screen = AppScreen.TABS }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Back",
                            tint = AmuletText
                        )
                    }
                    Text(
                        "Now Playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmuletTextMuted
                    )
                    IconButton(onClick = { screen = AppScreen.QUEUE }) {
                        Text("≡", color = AmuletText,
                            style = MaterialTheme.typography.titleLarge)
                    }
                }
                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    positionMs = position,
                    durationMs = duration,
                    onPlayPause = {
                        PlayerHolder.player?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                    },
                    onNext = { PlayerHolder.player?.seekToNextMediaItem() },
                    onPrevious = { PlayerHolder.player?.seekToPreviousMediaItem() },
                    onSeek = { PlayerHolder.player?.seekTo(it) },
                    onShuffleToggle = { PlaybackMode.toggleShuffle(PlayerHolder.player) },
                    onRepeatCycle = { PlaybackMode.cycleRepeat(PlayerHolder.player) },
                    onQueueClick = { screen = AppScreen.QUEUE },
                    onFavoriteClick = {
                        currentSong?.let { s ->
                            favorites = if (s.id in favorites)
                                favorites - s.id else favorites + s.id
                        }
                    },
                    isFavorite = currentSong?.id in favorites
                )
            }

            AppScreen.QUEUE -> Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { screen = AppScreen.PLAYER }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Back",
                            tint = AmuletText
                        )
                    }
                }
                QueueScreen(
                    queue = allSongs,
                    currentIndex = currentIndex,
                    onSongClick = { idx ->
                        PlayerHolder.player?.seekTo(idx, 0L)
                        PlayerHolder.player?.play()
                        screen = AppScreen.PLAYER
                    }
                )
            }

            AppScreen.TABS -> Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar(
                        containerColor = AmuletSurface,
                        tonalElevation = 0.dp
                    ) {
                        val navItems = listOf(
                            Triple("Home", Icons.Default.Home, 0),
                            Triple("Library", Icons.Default.LibraryMusic, 1),
                            Triple("Search", Icons.Default.Search, 2),
                            Triple("EQ", Icons.Default.Tune, 3),
                            Triple("More", Icons.Default.Settings, 4)
                        )
                        navItems.forEach { (label, icon, idx) ->
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
                        0 -> HomeScreen(
                            songs = allSongs,
                            onSongClick = ::playSong,
                            onDelete = ::requestDelete
                        )
                        1 -> LibraryScreen(
                            songs = allSongs,
                            onSongClick = ::playSong,
                            onDelete = ::requestDelete,
                            savedIndex = libraryScrollIndex,
                            savedOffset = libraryScrollOffset,
                            onScrollChanged = { idx, off ->
                                libraryScrollIndex = idx
                                libraryScrollOffset = off
                            }
                        )
                        2 -> SearchScreen(allSongs, ::playSong)
                        3 -> EqualizerScreen()
                        4 -> SettingsScreen()
                    }
                }
            }
        }
    }
}
