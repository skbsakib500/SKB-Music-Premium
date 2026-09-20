package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.player.SpatialAudioManager
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletBg
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletGold
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpatialScreen() {

    var mode by remember { mutableStateOf(SpatialAudioManager.currentMode) }
    var intensity by remember { mutableFloatStateOf(SpatialAudioManager.intensity) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Spatial Audio", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero ──
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AmuletEmerald.copy(alpha = 0.25f),
                                AmuletGold.copy(alpha = 0.15f),
                                AmuletBg
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "8 D   ·   1 0 D   ·   3 D",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmuletEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        modeLabel(mode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = AmuletText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Headphone recommended",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmuletTextMuted
                    )
                }
            }

            // ── Mode selector ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Mode", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                val modes = listOf(
                    "Off" to SpatialAudioManager.Mode.OFF,
                    "8D"  to SpatialAudioManager.Mode.ROTATE_8D,
                    "10D" to SpatialAudioManager.Mode.HYPER_10D,
                    "3D"  to SpatialAudioManager.Mode.BINAURAL_3D
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.forEach { (label, t) ->
                        val active = t == mode
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (active) AmuletEmerald.copy(alpha = 0.22f)
                                    else AmuletSurfaceHigh
                                )
                                .clickable {
                                    mode = t
                                    SpatialAudioManager.apply(t)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (active) AmuletEmerald else AmuletText,
                                fontWeight = if (active) FontWeight.Bold
                                else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Intensity slider
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Intensity", Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium)
                        Text("${(intensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmuletEmerald)
                    }
                    Slider(
                        value = intensity,
                        onValueChange = {
                            intensity = it
                            SpatialAudioManager.setIntensity(it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmuletEmerald,
                            activeTrackColor = AmuletEmerald,
                            inactiveTrackColor = AmuletDivider
                        )
                    )
                }
            }

            // ── Info ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("কীভাবে কাজ করে", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "• 8D — Virtualizer + Medium Room reverb\n" +
                    "• 10D — Deep Virtualizer + Large Hall + Bass\n" +
                    "• 3D — Subtle Virtualizer + Small Room\n\n" +
                    "সবগুলো Android-এর নিজস্ব AudioEffect API ব্যবহার করে — " +
                    "তাই সব ফোনে কাজ করবে।\n\n" +
                    "🎧 হেডফোন লাগালে সবচেয়ে ভালো শোনাবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmuletTextMuted
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun modeLabel(m: SpatialAudioManager.Mode): String = when (m) {
    SpatialAudioManager.Mode.OFF -> "Off"
    SpatialAudioManager.Mode.ROTATE_8D -> "8D Rotating"
    SpatialAudioManager.Mode.HYPER_10D -> "10D Hyper"
    SpatialAudioManager.Mode.BINAURAL_3D -> "3D Binaural"
}
