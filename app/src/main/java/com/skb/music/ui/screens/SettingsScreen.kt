package com.skb.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
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
                        Icons.Default.Palette,
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
                SettingRow(Icons.Default.Info, "About", "SKB Music")
                HorizontalDivider(
                    Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f)
                )
                SettingRow(Icons.Default.Code, "Version", "1.0.0")
                HorizontalDivider(
                    Modifier.padding(vertical = 12.dp),
                    color = AmuletEmerald.copy(alpha = 0.15f)
                )
                SettingRow(Icons.Default.Person, "Developer", "SKB")
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "© 2025 SKB Music",
                style = MaterialTheme.typography.labelSmall,
                color = AmuletTextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AmuletEmerald, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AmuletTextMuted)
    }
}
