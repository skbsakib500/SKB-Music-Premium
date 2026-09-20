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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.player.AudioProfileManager
import com.skb.music.player.SpatialAudioManager
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletSurfaceHigh
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpatialScreen() {
    var mode by remember { mutableStateOf(SpatialAudioManager.currentMode) }
    var level by remember { mutableFloatStateOf(SpatialAudioManager.level) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Spatial Audio", fontWeight = FontWeight.Bold,
                    color = AmuletText) },
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
            // Attach status
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (AudioProfileManager.isAttached)
                            "● Effects active" else "○ Start a song first",
                        color = if (AudioProfileManager.isAttached)
                            AmuletEmerald else AmuletTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            GlowCard(Modifier.fillMaxWidth()) {
                Text("Mode", fontWeight = FontWeight.SemiBold, color = AmuletText)
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

                Row(Modifier.fillMaxWidth()) {
                    Text("Intensity", Modifier.weight(1f),
                        color = AmuletText,
                        style = MaterialTheme.typography.bodyMedium)
                    Text("${(level * 100).toInt()}%",
                        color = AmuletEmerald,
                        style = MaterialTheme.typography.labelSmall)
                }
                Slider(
                    value = level,
                    onValueChange = {
                        level = it
                        SpatialAudioManager.applyLevel(it)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmuletEmerald,
                        activeTrackColor = AmuletEmerald,
                        inactiveTrackColor = AmuletDivider
                    )
                )
            }

            GlowCard(Modifier.fillMaxWidth()) {
                Text("কীভাবে কাজ করে", fontWeight = FontWeight.SemiBold,
                    color = AmuletText)
                Spacer(Modifier.height(8.dp))
                Text(
                    "• 8D → Virtualizer + Medium Room reverb\n" +
                    "• 10D → Virtualizer + Large Room + Bass\n" +
                    "• 3D → Subtle Virtualizer + Small Room\n\n" +
                    "🔊 একটা গান চালু করে তারপর মোড বদলান।\n" +
                    "🎧 হেডফোনে ভালো শোনাবে।\n\n" +
                    "কিছু ফোনে vendor Android এই effects বন্ধ রেখেছে — " +
                    "সেক্ষেত্রে কোনো পরিবর্তন শুনবেন না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmuletTextMuted
                )
            }
        }
    }
}
