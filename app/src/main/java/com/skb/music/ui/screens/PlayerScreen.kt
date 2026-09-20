package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skb.music.data.Song
import com.skb.music.player.PlaybackMode
import com.skb.music.ui.components.PaResources
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletGold
import com.skb.music.ui.theme.AmuletSurface
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleToggle: () -> Unit = {},
    onRepeatCycle: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(AmuletSurfaceHigh, AmuletSurface))),
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
                    painter = PaResources.musicNote(),
                    contentDescription = null,
                    tint = AmuletEmerald,
                    modifier = Modifier.size(96.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            song?.title ?: "Nothing Playing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AmuletText
        )
        Spacer(Modifier.height(4.dp))
        Text(
            song?.artist ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            color = AmuletTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(20.dp))

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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatTime(positionMs),
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted
            )
            Text(
                formatTime(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShuffleToggle) {
                Icon(
                    painter = PaResources.shuffle(),
                    contentDescription = "Shuffle",
                    tint = if (PlaybackMode.isShuffleOn.value) AmuletEmerald else AmuletTextMuted
                )
            }
            IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(
                    painter = PaResources.prev(),
                    contentDescription = "Previous",
                    tint = AmuletText,
                    modifier = Modifier.size(38.dp)
                )
            }
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AmuletEmerald, AmuletGold)))
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isPlaying) PaResources.pause() else PaResources.play(),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(
                    painter = PaResources.next(),
                    contentDescription = "Next",
                    tint = AmuletText,
                    modifier = Modifier.size(38.dp)
                )
            }
            IconButton(onClick = onRepeatCycle) {
                Icon(
                    painter = if (PlaybackMode.repeatMode.value == 2)
                        PaResources.repeatOne() else PaResources.repeat(),
                    contentDescription = "Repeat",
                    tint = if (PlaybackMode.repeatMode.value > 0) AmuletEmerald else AmuletTextMuted
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    painter = if (isFavorite) PaResources.heartFill()
                    else PaResources.heartOutline(),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) AmuletGold else AmuletTextMuted
                )
            }
            IconButton(onClick = onQueueClick) {
                Icon(
                    painter = PaResources.queue(),
                    contentDescription = "Queue",
                    tint = AmuletTextMuted
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
