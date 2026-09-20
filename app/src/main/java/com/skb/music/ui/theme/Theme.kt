package com.skb.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmuletColorScheme,
        content = content
    )
}
