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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.data.AutoEqApplier
import com.skb.music.player.AudioProfileManager
import com.skb.music.player.BTDeviceDetector
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.components.PaResources
import com.skb.music.ui.theme.AmuletDivider
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletGold
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioProfileScreen() {
    val context = LocalContext.current

    var selectedGenre by remember { mutableStateOf(AudioProfileManager.activeGenre) }
    var bass by remember { mutableFloatStateOf(AudioProfileManager.bassLevel) }
    var virt by remember { mutableFloatStateOf(AudioProfileManager.virtualLevel) }
    var loudness by remember { mutableIntStateOf(AudioProfileManager.loudnessGain) }
    var eqOn by remember { mutableStateOf(AudioProfileManager.eqEnabled) }

    LaunchedEffect(Unit) { BTDeviceDetector.refresh(context) }

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
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Now Playing Chain info ──
            val info = AudioProfileManager.currentInfo()
            GlowCard(Modifier.fillMaxWidth()) {
                Text(
                    "Active Audio Pipeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                InfoLine("Session", "#${info.sessionId}")
                InfoLine("EQ Bands", "${info.bands}")
                InfoLine("Headphone", info.headphone)
                InfoLine("Genre", info.genre)
                InfoLine("Bass", "${info.bassBoost}%")
                InfoLine("Virtual", "${info.virtualizer}%")
                InfoLine("Loudness", "${info.loudnessGain} mB")
                if (info.centerFreqs.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Freqs: " + info.centerFreqs.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = AmuletTextMuted
                    )
                }
            }

            // ── Bluetooth Headphone ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = PaResources.headphones(),
                        contentDescription = null,
                        tint = AmuletEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Connected Device",
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            BTDeviceDetector.connectedName.value ?: "None",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted
                        )
                    }
                }
                val guess = BTDeviceDetector.guessPresetFromDeviceName(
                    BTDeviceDetector.connectedName.value
                )
                if (guess != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                val bands = AutoEqApplier.bandsFor(context, guess)
                                AudioProfileManager.applyAutoEqBands(guess, bands)
                            }
                    ) {
                        Text(
                            "Apply AutoEq: $guess  →",
                            color = AmuletGold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Genre ──
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Genre EQ", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                AudioProfileManager.genres.chunked(2).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { g ->
                            val active = g == selectedGenre
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedGenre = g
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
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ── DSP Sliders ──
            GlowCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Equalizer ON", Modifier.weight(1f))
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
                HorizontalDivider(Modifier.padding(vertical = 12.dp),
                    color = AmuletDivider)
                SliderRow("Bass Boost", bass,
                    onChange = {
                        bass = it
                        AudioProfileManager.setBassStrength(it)
                    })
                SliderRow("Virtualizer", virt,
                    onChange = {
                        virt = it
                        AudioProfileManager.setVirtualStrength(it)
                    })
                SliderRow("Loudness Gain", loudness / 2000f,
                    onChange = {
                        val v = (it * 2000).toInt()
                        loudness = v
                        AudioProfileManager.applyLoudnessGain(v)
                    })
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Chaine: Bass → Virtual → EQ → Loudness\n" +
                        "Applied on Media3 ExoPlayer session",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = AmuletTextMuted, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall)
        Text(value, color = AmuletText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
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
