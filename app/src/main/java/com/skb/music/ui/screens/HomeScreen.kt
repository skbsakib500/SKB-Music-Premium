package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skb.music.R
import com.skb.music.data.MusicRepository
import com.skb.music.data.Song
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repo: MusicRepository,
    onSongClick: (Song, List<Song>) -> Unit
) {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        songs = repo.loadSongs()
        loading = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.skb_app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.skb_tagline),
                            style = MaterialTheme.typography.labelSmall,
                            color = AmuletEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> LoadingState(Modifier.align(Alignment.Center))
                songs.isEmpty() -> EmptyState(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    item { HeroCard(songs.size) }
                    item {
                        Row(
                            Modifier.fillMaxWidth()
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
                    items(songs, key = { it.id }) { s ->
                        SongRow(s) { onSongClick(s, songs) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(count: Int) {
    GlowCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(AmuletEmerald, AmuletGold))),
                contentAlignment = Alignment.Center
            ) {
                // Poweramp-এর আইকন ব্যবহার করছি!
                Icon(
                    painter = painterResource(R.drawable.skb_ic_play),
                    contentDescription = null,
                    tint = Color.Black,
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
                Text(
                    "$count songs ready",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmuletTextMuted
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmuletEmerald,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.skb_ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Play All")
            }
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.skb_ic_shuffle),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
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
            painter = painterResource(R.drawable.skb_ic_play),
            contentDescription = null,
            tint = AmuletTextMuted,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("No songs found", color = AmuletTextMuted)
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
            shape = RoundedCornerShape(12.dp),
            color = AmuletSurfaceHigh,
            modifier = Modifier.size(48.dp)
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(model = song.albumArtUri, contentDescription = null)
            } else {
                Icon(
                    painter = painterResource(R.drawable.skb_ic_play),
                    contentDescription = null,
                    tint = AmuletEmerald,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = AmuletTextMuted,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        // Poweramp-এর heart icon!
        Icon(
            painter = painterResource(R.drawable.skb_ic_favorite_outline),
            contentDescription = null,
            tint = AmuletEmerald,
            modifier = Modifier.size(20.dp)
        )
    }
}
