package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.skb.music.data.Song
import com.skb.music.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Album art with emerald glow
        Box(
            Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(AmuletSurfaceHigh, AmuletSurface))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song?.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = AmuletEmerald
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            song?.title ?: "Nothing Playing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            song?.artist ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            color = AmuletTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(32.dp))

        // Seekbar
        var dragValue by remember { mutableStateOf<Float?>(null) }
        val progress = dragValue ?: if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { onSeek((it * durationMs).toLong()) }
                dragValue = null
            },
            colors = SliderDefaults.colors(
                thumbColor = AmuletEmerald,
                activeTrackColor = AmuletEmerald,
                inactiveTrackColor = AmuletDivider
            )
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(positionMs), style = MaterialTheme.typography.labelSmall, color = AmuletTextMuted)
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall, color = AmuletTextMuted)
        }

        Spacer(Modifier.height(24.dp))

        // Controls
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = AmuletText,
                    modifier = Modifier.size(36.dp)
                )
            }

            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AmuletEmerald, AmuletGold))),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = androidx.compose.ui.graphics.Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = AmuletText,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
