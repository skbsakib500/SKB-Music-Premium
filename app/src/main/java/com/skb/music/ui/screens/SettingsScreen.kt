package com.skb.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.R
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                        painter = painterResource(R.drawable.skb_ic_settings_look_feel_colored),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Theme", fontWeight = FontWeight.SemiBold)
                        Text("Amulet (Premium Dark)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmuletTextMuted)
                    }
                    Text("✓", color = AmuletEmerald)
                }
            }

            GlowCard(Modifier.fillMaxWidth()) {
                SettingRow(R.drawable.skb_ic_settings_audio_colored, "Audio", "Output, DVC")
                HorizontalDivider(Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f))
                SettingRow(R.drawable.skb_ic_settings_equ_colored, "Equalizer", "Bands, presets")
                HorizontalDivider(Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f))
                SettingRow(R.drawable.skb_ic_settings_headset_colored, "Headset", "Buttons, resume")
                HorizontalDivider(Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f))
                SettingRow(R.drawable.skb_ic_settings_vis_colored, "Visualization", "Milkdrop, bars")
                HorizontalDivider(Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f))
                SettingRow(R.drawable.skb_ic_settings_folders_library_colored, "Library", "Scan, folders")
            }

            // Poweramp-এর আসল menu XML ব্যবহার করছি!
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Poweramp widget menu preview",
                    fontWeight = FontWeight.SemiBold,
                    color = AmuletTextMuted)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))
            Text("© 2025 SKB Music",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun SettingRow(iconRes: Int, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AmuletTextMuted)
    }
}
