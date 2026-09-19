package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skb.music.data.MusicRepository
import com.skb.music.data.Song
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletGold
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletTextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repo: MusicRepository,
    onSongClick: (Song, List<Song>) -> Unit
) {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        songs = repo.loadSongs()
        loading = false
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SKB Music",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Premium Player",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmuletEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> LoadingState(Modifier.align(Alignment.Center))
                songs.isEmpty() -> EmptyState(Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item { HeroCard(songs.size) }

                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "All Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${songs.size} tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmuletTextMuted
                                )
                            }
                        }

                        items(songs, key = { it.id }) { song ->
                            SongRow(song) { onSongClick(song, songs) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(count: Int) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(AmuletEmerald, AmuletGold))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Your Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "$count songs ready to play",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmuletTextMuted
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { /* handled by parent */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmuletEmerald,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Play All")
            }
            OutlinedButton(
                onClick = { /* shuffle */ },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Shuffle")
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = AmuletEmerald)
        Spacer(Modifier.height(16.dp))
        Text("Scanning library…", color = AmuletTextMuted)
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = AmuletTextMuted
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No songs found",
            style = MaterialTheme.typography.titleMedium,
            color = AmuletTextMuted
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add music to your device and reopen",
            style = MaterialTheme.typography.bodySmall,
            color = AmuletTextMuted
        )
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AmuletSurfaceHigh,
            modifier = Modifier.size(52.dp)
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AmuletEmerald,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = AmuletTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = AmuletEmerald
        )
    }
}
