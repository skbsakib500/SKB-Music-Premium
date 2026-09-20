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
import com.skb.music.player.spatial.SpatialMode
import com.skb.music.player.spatial.SpatialType
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

    var type by remember { mutableStateOf(SpatialMode.type.value) }

    // 8D
    var r8speed by remember { mutableFloatStateOf(SpatialMode.rotateSpeed.value) }
    var r8wet   by remember { mutableFloatStateOf(SpatialMode.rotateWet.value) }
    var r8rad   by remember { mutableFloatStateOf(SpatialMode.rotateRadius.value) }

    // 10D
    var hSpeed by remember { mutableFloatStateOf(SpatialMode.hyperSpeed.value) }
    var hWet   by remember { mutableFloatStateOf(SpatialMode.hyperWet.value) }
    var hDepth by remember { mutableFloatStateOf(SpatialMode.hyperDepth.value) }
    var hHaas  by remember { mutableFloatStateOf(SpatialMode.hyperHaas.value) }

    // 3D
    var bWidth by remember { mutableFloatStateOf(SpatialMode.binauralWidth.value) }
    var bDepth by remember { mutableFloatStateOf(SpatialMode.binauralDepth.value) }
    var bElev  by remember { mutableFloatStateOf(SpatialMode.binauralElev.value) }

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
                        SpatialMode.label(),
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
                    "Off"        to SpatialType.OFF,
                    "8D"         to SpatialType.SPATIAL_8D,
                    "10D"        to SpatialType.SPATIAL_10D,
                    "3D Binaural" to SpatialType.BINAURAL_3D
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.forEach { (label, t) ->
                        val active = t == type
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (active) AmuletEmerald.copy(alpha = 0.22f)
                                    else AmuletSurfaceHigh
                                )
                                .clickable {
                                    type = t
                                    SpatialMode.setType(t)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (active) AmuletEmerald else AmuletText,
                                fontWeight = if (active) FontWeight.Bold
                                else FontWeight.Normal,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Mode change → অ্যাপ রিস্টার্ট করুন",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmuletTextMuted
                )
            }

            // ── 8D controls ──
            if (type == SpatialType.SPATIAL_8D) {
                GlowCard(Modifier.fillMaxWidth()) {
                    Text("8D Controls", fontWeight = FontWeight.SemiBold)
                    Sld("Rotation Speed", r8speed, 0.01f, 0.5f,
                        "${(r8speed * 100).toInt() / 100f} Hz") {
                        r8speed = it; SpatialMode.rotateSpeed.value = it
                    }
                    Sld("Wet Mix", r8wet, 0f, 0.6f,
                        "${(r8wet * 100).toInt()}%") {
                        r8wet = it; SpatialMode.rotateWet.value = it
                    }
                    Sld("Radius", r8rad, 0f, 1f,
                        "${(r8rad * 100).toInt()}%") {
                        r8rad = it; SpatialMode.rotateRadius.value = it
                    }
                }
            }

            // ── 10D controls ──
            if (type == SpatialType.SPATIAL_10D) {
                GlowCard(Modifier.fillMaxWidth()) {
                    Text("10D Hyper Controls", fontWeight = FontWeight.SemiBold)
                    Sld("Rotation Speed", hSpeed, 0.01f, 0.3f,
                        "${(hSpeed * 100).toInt() / 100f} Hz") {
                        hSpeed = it; SpatialMode.hyperSpeed.value = it
                    }
                    Sld("Wet Mix", hWet, 0f, 0.6f,
                        "${(hWet * 100).toInt()}%") {
                        hWet = it; SpatialMode.hyperWet.value = it
                    }
                    Sld("Binaural Depth", hDepth, 0f, 1f,
                        "${(hDepth * 100).toInt()}%") {
                        hDepth = it; SpatialMode.hyperDepth.value = it
                    }
                    Sld("Haas Delay", hHaas, 0f, 30f,
                        "${hHaas.toInt()} ms") {
                        hHaas = it; SpatialMode.hyperHaas.value = it
                    }
                }
            }

            // ── 3D controls ──
            if (type == SpatialType.BINAURAL_3D) {
                GlowCard(Modifier.fillMaxWidth()) {
                    Text("3D Binaural Controls", fontWeight = FontWeight.SemiBold)
                    Sld("Stereo Width", bWidth, 0f, 1f,
                        "${(bWidth * 100).toInt()}%") {
                        bWidth = it; SpatialMode.binauralWidth.value = it
                    }
                    Sld("Room Depth", bDepth, 0f, 1f,
                        "${(bDepth * 100).toInt()}%") {
                        bDepth = it; SpatialMode.binauralDepth.value = it
                    }
                    Sld("Elevation", bElev, -1f, 1f,
                        "${(bElev * 100).toInt()}%") {
                        bElev = it; SpatialMode.binauralElev.value = it
                    }
                }
            }

            // ── Explanation ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("কীভাবে কাজ করে", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• 8D — একটি mono source বৃত্তে ঘোরে + reverb tail\n" +
                    "• 10D — 8D + HRTF + Haas delay + room simulation\n" +
                    "• 3D — বাইনারাল ITD/ILD, হেডফোনে আসল depth\n\n" +
                    "হেডফোন ছাড়া ভালো শোনাবে না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmuletTextMuted
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Sld(
    label: String, value: Float, min: Float, max: Float, display: String,
    onChange: (Float) -> Unit
) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
            Text(display, style = MaterialTheme.typography.labelSmall,
                color = AmuletEmerald)
        }
        Slider(
            value = value.coerceIn(min, max),
            onValueChange = onChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = AmuletEmerald,
                activeTrackColor = AmuletEmerald,
                inactiveTrackColor = AmuletDivider
            )
        )
    }
}
