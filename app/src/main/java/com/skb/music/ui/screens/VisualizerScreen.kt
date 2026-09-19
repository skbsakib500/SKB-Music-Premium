package com.skb.music.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skb.music.ui.components.GlowCard
import com.skb.music.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen() {
    val context = LocalContext.current
    val presets = remember {
        runCatching {
            context.assets.list("milk_presets")?.toList() ?: emptyList()
        }.getOrDefault(emptyList())
    }
    var selected by remember { mutableStateOf(presets.firstOrNull() ?: "") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Visualizer", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
        ) {
            // Animated bars
            GlowCard(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp)
            ) {
                BarVisualizer(Modifier.fillMaxSize())
            }

            Text(
                "Milk Presets (${presets.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (presets.isEmpty()) {
                Text(
                    "No presets found in assets/milk_presets",
                    color = AmuletTextMuted,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(presets) { p ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected == p,
                                onClick = { selected = p },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AmuletEmerald,
                                    unselectedColor = AmuletTextMuted
                                )
                            )
                            Text(
                                p.removeSuffix(".milk"),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarVisualizer(modifier: Modifier = Modifier) {
    val bars = 32
    val values = remember { List(bars) { Animatable(0.1f) } }

    LaunchedEffect(Unit) {
        while (true) {
            values.forEachIndexed { i, anim ->
                val target = (0.2f + Math.random().toFloat() * 0.8f) *
                        (1f - i.toFloat() / (bars * 2f))
                anim.animateTo(
                    target,
                    animationSpec = tween(durationMillis = 400)
                )
            }
            delay(300)
        }
    }

    Canvas(modifier) {
        val barW = size.width / (bars * 1.5f)
        val gap = barW * 0.5f
        values.forEachIndexed { i, anim ->
            val h = size.height * anim.value
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(AmuletGold, AmuletEmerald)),
                topLeft = Offset(i * (barW + gap), size.height - h),
                size = Size(barW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2)
            )
        }
    }
}
