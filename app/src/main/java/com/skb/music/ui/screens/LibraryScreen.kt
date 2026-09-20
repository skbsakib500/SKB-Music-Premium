package com.skb.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.skb.music.ui.theme.AmuletTextMuted

@Composable
fun LibraryScreen(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onDelete: (Song) -> Unit,
    savedIndex: Int = 0,
    savedOffset: Int = 0,
    onScrollChanged: (Int, Int) -> Unit = { _, _ -> }
) {
    var tab by remember { mutableIntStateOf(0) }
    var pendingDelete by remember { mutableStateOf<Song?>(null) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedIndex,
        initialFirstVisibleItemScrollOffset = savedOffset
    )

    // Save scroll position continuously
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (idx, off) -> onScrollChanged(idx, off) }
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            contentColor = AmuletEmerald
        ) {
            listOf("Songs", "Albums", "Artists").forEachIndexed { i, name ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Text(name, fontWeight = if (tab == i) FontWeight.SemiBold
                        else FontWeight.Normal)
                    }
                )
            }
        }

        when (tab) {
            0 -> LazyColumn(
                Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                items(songs, key = { it.id }) { s ->
                    SongRow(s, { onSongClick(s, songs) }, { pendingDelete = s })
                }
            }
            1 -> AlbumsList(songs, onSongClick)
            2 -> ArtistsList(songs, onSongClick)
        }
    }

    pendingDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete permanently?") },
            text = { Text("'${song.title}' ফাইল থেকে মুছে যাবে।") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(song)
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit, onDeleteClick: () -> Unit) {
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
            modifier = Modifier.size(48.dp)
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(model = song.albumArtUri, contentDescription = null)
            } else {
                Icon(
                    painter = PaResources.musicNote(),
                    contentDescription = null,
                    tint = AmuletEmerald,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF0F4F8), fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF10D9A0), maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Text("⋮", color = AmuletTextMuted,
                    style = MaterialTheme.typography.titleLarge)
            }
            DropdownMenu(expanded = menuOpen,
                onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Delete permanently",
                        color = Color(0xFFFF6B6B)) },
                    onClick = { menuOpen = false; onDeleteClick() }
                )
            }
        }
    }
}

@Composable
private fun AlbumsList(songs: List<Song>, onClick: (Song, List<Song>) -> Unit) {
    val grouped = songs.groupBy { it.album }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 140.dp)) {
        items(grouped.keys.toList()) { album ->
            val albumSongs = grouped[album].orEmpty()
            Row(
                Modifier.fillMaxWidth()
                    .clickable { albumSongs.firstOrNull()?.let { onClick(it, albumSongs) } }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = AmuletSurfaceHigh,
                    modifier = Modifier.size(56.dp)) {
                    val art = albumSongs.firstOrNull()?.albumArtUri
                    if (art != null) AsyncImage(model = art, contentDescription = null)
                    else Icon(painter = PaResources.musicNote(), contentDescription = null,
                        tint = AmuletEmerald, modifier = Modifier.padding(14.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(album, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("${albumSongs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmuletTextMuted)
                }
            }
        }
    }
}

@Composable
private fun ArtistsList(songs: List<Song>, onClick: (Song, List<Song>) -> Unit) {
    val grouped = songs.groupBy { it.artist }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 140.dp)) {
        items(grouped.keys.toList()) { artist ->
            val artistSongs = grouped[artist].orEmpty()
            Row(
                Modifier.fillMaxWidth()
                    .clickable { artistSongs.firstOrNull()?.let { onClick(it, artistSongs) } }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(50), color = AmuletSurfaceHigh,
                    modifier = Modifier.size(48.dp)) {
                    Icon(painter = PaResources.headphones(), contentDescription = null,
                        tint = AmuletEmerald, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(artist, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("${artistSongs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmuletTextMuted)
                }
            }
        }
    }
}
