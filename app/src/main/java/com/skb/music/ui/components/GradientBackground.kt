package com.skb.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.skb.music.ui.theme.AmuletBg
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletViolet

/**
 * Deep obsidian background with subtle emerald/violet radial glow.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        AmuletEmerald.copy(alpha = 0.10f),
                        AmuletViolet.copy(alpha = 0.05f),
                        Color.Transparent,
                        AmuletBg
                    ),
                    radius = 900f
                )
            )
    ) {
        content()
    }
}
