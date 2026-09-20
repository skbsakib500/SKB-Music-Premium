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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import com.skb.music.player.HiResSettings
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
fun HiResScreen() {
    var enabled by remember { mutableStateOf(HiResSettings.enabled.value) }
    var factor by remember { mutableStateOf(HiResSettings.factor.value) }
    var float32 by remember { mutableStateOf(HiResSettings.float32.value) }

    val commonInputs = listOf(44100, 48000, 96000, 192000)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Hi-Res Audio", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero ──
            Box(
                Modifier
                    .fillMaxWidth()
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
                        "32-bit / 768 kHz",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = AmuletText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (enabled) "ACTIVE · ${factor}x upsampling · float32"
                        else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (enabled) AmuletEmerald else AmuletTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Master toggle ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hi-Res Pipeline",
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            "Catmull-Rom cubic upsampler + 32-bit float",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            HiResSettings.enabled.value = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmuletEmerald,
                            checkedTrackColor = AmuletEmerald.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // ── Upsample factor ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Upsample Factor", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 4, 8).forEach { f ->
                        val active = f == factor
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (active) AmuletEmerald.copy(alpha = 0.25f)
                                    else AmuletSurfaceHigh
                                )
                                .clickable {
                                    factor = f
                                    HiResSettings.factor.value = f
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${f}x",
                                color = if (active) AmuletEmerald else AmuletText,
                                fontWeight = if (active) FontWeight.Bold
                                else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // ── Float toggle ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("32-bit Float Output",
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            "Feeds ENCODING_PCM_FLOAT to AudioTrack",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted
                        )
                    }
                    Switch(
                        checked = float32,
                        onCheckedChange = {
                            float32 = it
                            HiResSettings.float32.value = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmuletGold,
                            checkedTrackColor = AmuletGold.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // ── Effective output rate preview ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Pipeline Preview", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AmuletDivider)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Input → Output (when Hi-Res ON)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmuletTextMuted
                )
                Spacer(Modifier.height(6.dp))
                commonInputs.forEach { inRate ->
                    val out = if (enabled) HiResSettings.outputRate(inRate) else inRate
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(
                            "${inRate / 1000} kHz",
                            Modifier.width(80.dp),
                            color = AmuletTextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "→",
                            Modifier.width(20.dp),
                            color = AmuletTextMuted
                        )
                        Text(
                            "${out / 1000} kHz · float32",
                            color = if (out > inRate) AmuletEmerald else AmuletText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (out > inRate) FontWeight.Bold
                            else FontWeight.Normal
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AmuletDivider)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Note: Actual DAC output depends on device. " +
                    "The internal pipeline always runs at the configured rate.",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmuletTextMuted
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
