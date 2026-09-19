package com.skb.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Amulet is a dark, mystical, always-dark theme
private val AmuletColorScheme = darkColorScheme(
    primary = AmuletEmerald,
    onPrimary = AmuletBg,
    primaryContainer = AmuletEmeraldDark,
    onPrimaryContainer = AmuletText,

    secondary = AmuletGold,
    onSecondary = AmuletBg,

    tertiary = AmuletViolet,
    onTertiary = AmuletText,

    background = AmuletBg,
    onBackground = AmuletText,

    surface = AmuletSurface,
    onSurface = AmuletText,

    surfaceVariant = AmuletSurfaceHigh,
    onSurfaceVariant = AmuletTextMuted,

    outline = AmuletDivider,
    error = AmuletError,
    onError = AmuletBg
)

@Composable
fun SKBMusicTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Amulet theme is always dark
    MaterialTheme(
        colorScheme = AmuletColorScheme,
        content = content
    )
}
