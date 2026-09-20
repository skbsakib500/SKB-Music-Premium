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
import com.skb.music.player.AudioPipeline
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
fun AdvancedAudioScreen() {

    var dsp by remember { mutableStateOf(AudioPipeline.dspEnabled.value) }
    var preamp by remember { mutableFloatStateOf(AudioPipeline.preampDb.value) }
    var headroom by remember { mutableFloatStateOf(AudioPipeline.headroomDb.value) }
    var width by remember { mutableFloatStateOf(AudioPipeline.stereoWidth.value) }
    var crossfeed by remember { mutableFloatStateOf(AudioPipeline.crossfeed.value) }
    var bass by remember { mutableFloatStateOf(AudioPipeline.dynBass.value) }
    var bassGain by remember { mutableFloatStateOf(AudioPipeline.dynBassGain.value) }
    var exciter by remember { mutableFloatStateOf(AudioPipeline.exciter.value) }
    var limiter by remember { mutableStateOf(AudioPipeline.limiterOn.value) }
    var activePreset by remember { mutableStateOf("Reference") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Advanced Audio", fontWeight = FontWeight.Bold) },
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
                        "A D V A N C E D   D S P",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmuletEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "6-stage pipeline",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = AmuletText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (dsp) "ACTIVE · 32-bit float" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dsp) AmuletEmerald else AmuletTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Master switch ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("DSP Pipeline",
                            fontWeight = FontWeight.SemiBold)
                        Text("অ্যাপ রিস্টার্টে কার্যকর হবে",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted)
                    }
                    Switch(
                        checked = dsp,
                        onCheckedChange = {
                            dsp = it
                            AudioPipeline.dspEnabled.value = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmuletEmerald,
                            checkedTrackColor = AmuletEmerald.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // ── Presets ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Presets", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                AudioPipeline.presets.chunked(2).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { p ->
                            val active = p.name == activePreset
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (active) AmuletEmerald.copy(alpha = 0.22f)
                                        else AmuletSurfaceHigh
                                    )
                                    .clickable {
                                        activePreset = p.name
                                        AudioPipeline.applyPreset(p)
                                        preamp = p.preamp
                                        headroom = p.headroom
                                        width = p.width
                                        crossfeed = p.crossfeed
                                        bass = p.bass
                                        bassGain = p.bassGain
                                        exciter = p.exciter
                                        limiter = p.limiter
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    p.name,
                                    color = if (active) AmuletEmerald else AmuletText,
                                    fontWeight = if (active) FontWeight.Bold
                                    else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ── Preamp ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Stage 1 · Preamp", fontWeight = FontWeight.SemiBold)
                SliderRow("Gain", preamp, -12f, 12f, "${preamp.toInt()} dB") {
                    preamp = it; AudioPipeline.preampDb.value = it
                }
                SliderRow("Headroom", headroom, -6f, 0f, "${headroom.toInt()} dB") {
                    headroom = it; AudioPipeline.headroomDb.value = it
                }
            }

            // ── Stereo Width ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Stage 2 · Stereo Width", fontWeight = FontWeight.SemiBold)
                SliderRow("Width", width, 0f, 2f, "${(width * 100).toInt()}%") {
                    width = it; AudioPipeline.stereoWidth.value = it
                }
            }

            // ── Crossfeed ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Stage 3 · Crossfeed", fontWeight = FontWeight.SemiBold)
                SliderRow("Amount", crossfeed, 0f, 1f, "${(crossfeed * 100).toInt()}%") {
                    crossfeed = it; AudioPipeline.crossfeed.value = it
                }
            }

            // ── Dynamic Bass ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Stage 4 · Dynamic Bass", fontWeight = FontWeight.SemiBold)
                SliderRow("Amount", bass, 0f, 1f, "${(bass * 100).toInt()}%") {
                    bass = it; AudioPipeline.dynBass.value = it
                }
                SliderRow("Gain", bassGain, 0f, 12f, "+${bassGain.toInt()} dB") {
                    bassGain = it; AudioPipeline.dynBassGain.value = it
                }
            }

            // ── Exciter ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Stage 5 · Harmonic Exciter", fontWeight = FontWeight.SemiBold)
                SliderRow("Amount", exciter, 0f, 1f, "${(exciter * 100).toInt()}%") {
                    exciter = it; AudioPipeline.exciter.value = it
                }
            }

            // ── Limiter ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Stage 6 · Soft Limiter",
                            fontWeight = FontWeight.SemiBold)
                        Text("Peaks controlled without distortion",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted)
                    }
                    Switch(
                        checked = limiter,
                        onCheckedChange = {
                            limiter = it; AudioPipeline.limiterOn.value = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmuletGold,
                            checkedTrackColor = AmuletGold.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Chain:  Preamp → Width → Crossfeed → DynBass → Exciter → Limiter\n" +
                "Sample fmt: ENCODING_PCM_FLOAT (32-bit)",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String, value: Float,
    min: Float, max: Float, display: String,
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
