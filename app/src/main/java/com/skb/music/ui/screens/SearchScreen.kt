package com.skb.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skb.music.data.Song
import com.skb.music.ui.components.PaResources
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@Composable
fun SearchScreen(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, songs) {
        if (query.isBlank()) emptyList()
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search songs, artists…") },
            leadingIcon = {
                Icon(
                    painter = PaResources.search(),
                    contentDescription = null,
                    tint = AmuletEmerald
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmuletEmerald,
                unfocusedBorderColor = AmuletDivider,
                focusedTextColor = AmuletText,
                unfocusedTextColor = AmuletText,
                cursorColor = AmuletEmerald
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(8.dp))
        when {
            query.isBlank() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Start typing to search", color = AmuletTextMuted)
            }
            results.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No results for \"$query\"", color = AmuletTextMuted)
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(results, key = { it.id }) { s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(s, results) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmuletSurfaceHigh,
                            modifier = Modifier.size(44.dp)
                        ) {
                            if (s.albumArtUri != null) {
                                AsyncImage(
                                    model = s.albumArtUri,
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    painter = PaResources.musicNote(),
                                    contentDescription = null,
                                    tint = AmuletEmerald,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.title,
                                color = AmuletText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                s.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = AmuletTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
