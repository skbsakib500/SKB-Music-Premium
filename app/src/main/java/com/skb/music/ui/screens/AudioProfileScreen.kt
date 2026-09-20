package com.skb.music.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.player.AudioProfileManager
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioProfileScreen() {
    var genre by remember { mutableStateOf(AudioProfileManager.activeGenre) }
    var bass by remember { mutableFloatStateOf(AudioProfileManager.bassLevel) }
    var virt by remember { mutableFloatStateOf(AudioProfileManager.virtualLevel) }
    var loud by remember { mutableIntStateOf(AudioProfileManager.loudnessMb) }
    var eqOn by remember { mutableStateOf(AudioProfileManager.eqEnabled) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Audio Profile", fontWeight = FontWeight.Bold) },
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

            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Equalizer", fontWeight = FontWeight.SemiBold,
                            color = AmuletText)
                        Text("5-band system equalizer",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted)
                    }
                    Switch(
                        checked = eqOn,
                        onCheckedChange = {
                            eqOn = it
                            AudioProfileManager.toggleEq(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmuletEmerald,
                            checkedTrackColor = AmuletEmerald.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            GlowCard(Modifier.fillMaxWidth()) {
                Text("Genre", fontWeight = FontWeight.SemiBold, color = AmuletText)
                Spacer(Modifier.height(8.dp))
                AudioProfileManager.genres.chunked(2).forEach { pair ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { g ->
                            val active = g == genre
                            Box(
                                Modifier.weight(1f)
                                    .clickable {
                                        genre = g
                                        AudioProfileManager.applyGenre(g)
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    g,
                                    color = if (active) AmuletEmerald else AmuletText,
                                    fontWeight = if (active) FontWeight.Bold
                                    else FontWeight.Normal
                                )
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            GlowCard(Modifier.fillMaxWidth()) {
                SliderRow("Bass Boost", bass) {
                    bass = it
                    AudioProfileManager.setBassStrength(it)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp),
                    color = AmuletDivider)
                SliderRow("Virtualizer", virt) {
                    virt = it
                    AudioProfileManager.setVirtualStrength(it)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp),
                    color = AmuletDivider)
                SliderRow("Loudness", loud / 2000f) {
                    val v = (it * 2000).toInt()
                    loud = v
                    AudioProfileManager.applyLoudnessGain(v)
                }
            }

            Text(
                "AudioEffect API • per-session DSP",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = AmuletText)
            Text("${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletEmerald)
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = AmuletEmerald,
                activeTrackColor = AmuletEmerald,
                inactiveTrackColor = AmuletDivider
            )
        )
    }
}
