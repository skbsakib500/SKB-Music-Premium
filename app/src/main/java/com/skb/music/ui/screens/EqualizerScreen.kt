package com.skb.music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.player.EqualizerManager
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen() {
    var selected by remember { mutableStateOf(EqualizerManager.current()) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            EqualizerManager.presets.forEach { name ->
                GlowCard(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = name
                            EqualizerManager.applyPreset(name)
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selected == name,
                            onClick = {
                                selected = name
                                EqualizerManager.applyPreset(name)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AmuletEmerald
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "5-band equalizer powered by Android AudioEffect",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted
            )
        }
    }
}
