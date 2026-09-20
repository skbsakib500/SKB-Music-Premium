package com.skb.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.components.PaResources
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var sub by remember { mutableStateOf<String?>(null) }

    when (sub) {
        "autoeq" -> AutoEqScreen()
        "audio_profile" -> AudioProfileScreen()
        "hires" -> HiResScreen()
        "visualizer" -> VisualizerScreen()
        "equalizer" -> EqualizerScreen()
        else -> Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("More", fontWeight = FontWeight.Bold) },
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
                GlowCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = PaResources.settings(),
                            contentDescription = null,
                            tint = AmuletEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Theme", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Amulet (Premium Dark)",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmuletTextMuted
                            )
                        }
                        Text("✓", color = AmuletEmerald)
                    }
                }

                GlowCard(Modifier.fillMaxWidth()) {
                    RowItem(PaResources.eq(), "Hi-Res Audio", "32-bit / 768 kHz") { sub = "hires" }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AmuletEmerald.copy(alpha = 0.15f))
                    RowItem(PaResources.eq(), "Audio Profile", "DSP chain") { sub = "audio_profile" }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AmuletEmerald.copy(alpha = 0.15f))
                    RowItem(PaResources.eq(), "Equalizer",
                        "Bands, presets") { sub = "equalizer" }
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = AmuletEmerald.copy(alpha = 0.15f)
                    )
                    RowItem(PaResources.headphones(), "AutoEq",
                        "Headphone presets") { sub = "autoeq" }
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = AmuletEmerald.copy(alpha = 0.15f)
                    )
                    RowItem(PaResources.tune(), "Visualizer",
                        "Bars, milk presets") { sub = "visualizer" }
                }

                GlowCard(Modifier.fillMaxWidth()) {
                    InfoRow("About", "SKB Music")
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = AmuletEmerald.copy(alpha = 0.15f)
                    )
                    InfoRow("Version", "1.0.0")
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = AmuletEmerald.copy(alpha = 0.15f)
                    )
                    InfoRow("Developer", "SKB")
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "© 2025 SKB Music",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmuletTextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun RowItem(icon: Painter, title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = AmuletEmerald,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(">", color = AmuletTextMuted)
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AmuletTextMuted)
    }
}
