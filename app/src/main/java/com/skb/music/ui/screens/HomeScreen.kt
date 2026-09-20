package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skb.music.data.Song
import com.skb.music.ui.components.PaResources
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onDelete: (Song) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SKB Music",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmuletText
                        )
                        Text(
                            "Premium Player",
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
            if (songs.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = PaResources.musicNote(),
                        contentDescription = null,
                        tint = AmuletTextMuted,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No songs found", color = AmuletTextMuted)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                ) {
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
                                fontWeight = FontWeight.SemiBold,
                                color = AmuletText
                            )
                            Text(
                                "${songs.size} tracks",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmuletTextMuted
                            )
                        }
                    }
                    items(songs, key = { it.id }) { s ->
                        SongRow(
                            song = s,
                            onClick = { onSongClick(s, songs) },
                            onDeleteClick = { pendingDelete = s }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = AmuletSurfaceHigh,
            title = { Text("Delete permanently?", color = AmuletText) },
            text = {
                Text(
                    "'${song.title}' ফাইল থেকে মুছে যাবে। এটা ফিরিয়ে আনা যাবে না।",
                    color = AmuletTextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(song)
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = AmuletTextMuted)
                }
            }
        )
    }
}

@Composable
private fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

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
                    painter = PaResources.musicNote(),
                    contentDescription = null,
                    tint = AmuletEmerald,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF0F4F8),   // ← স্পষ্ট সাদা
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF10D9A0),   // ← এমারেল্ড সবুজ
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { menuOpen = true }) {
                Text("⋮", color = AmuletTextMuted,
                    style = MaterialTheme.typography.titleLarge)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete permanently",
                        color = Color(0xFFFF6B6B)) },
                    onClick = {
                        menuOpen = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}
