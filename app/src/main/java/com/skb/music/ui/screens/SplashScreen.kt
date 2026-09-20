package com.skb.music.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skb.music.ui.theme.AmuletBg
import com.skb.music.ui.theme.AmuletEmerald
import com.skb.music.ui.theme.AmuletGold
import com.skb.music.ui.theme.AmuletText
import com.skb.music.ui.theme.AmuletTextMuted
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // Master timeline: 0 → 1 over 2.4s
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    // Continuous rotation for outer ring
    val infinite = rememberInfiniteTransition(label = "splash")
    val ringRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // When done, call onFinished after short hold
    LaunchedEffect(progress.value) {
        if (progress.value >= 1f) {
            delay(200)
            onFinished()
        }
    }

    val p = progress.value

    // Animation stages:
    // 0.00 - 0.35 : ring draws
    // 0.35 - 0.65 : gem scales in
    // 0.60 - 0.85 : "SKB" text fades
    // 0.75 - 1.00 : "Sakib" text fades
    // 0.90 - 1.00 : tagline fades

    val ringProgress = (p / 0.35f).coerceIn(0f, 1f)
    val gemProgress = ((p - 0.30f) / 0.35f).coerceIn(0f, 1f)
    val skbAlpha = ((p - 0.55f) / 0.25f).coerceIn(0f, 1f)
    val sakibAlpha = ((p - 0.72f) / 0.20f).coerceIn(0f, 1f)
    val tagAlpha = ((p - 0.88f) / 0.12f).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        AmuletEmerald.copy(alpha = 0.10f * p),
                        Color.Transparent,
                        AmuletBg
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Emblem ──
            Box(
                Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseR = size.minDimension / 2f

                    // Outer rotating ring
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                AmuletEmerald,
                                AmuletGold,
                                AmuletEmerald.copy(alpha = 0.2f),
                                AmuletEmerald
                            )
                        ),
                        startAngle = ringRotation,
                        sweepAngle = 360f * ringProgress,
                        useCenter = false,
                        topLeft = Offset(center.x - baseR * 0.95f, center.y - baseR * 0.95f),
                        size = androidx.compose.ui.geometry.Size(baseR * 1.9f, baseR * 1.9f),
                        style = Stroke(width = 3f)
                    )

                    // Glow ring (pulsing)
                    drawCircle(
                        color = AmuletEmerald.copy(alpha = 0.15f * p * pulse),
                        radius = baseR * 0.75f * pulse,
                        center = center,
                        style = Stroke(width = 6f)
                    )

                    // Gem diamond
                    if (gemProgress > 0f) {
                        val g = baseR * 0.55f * gemProgress
                        val pts = listOf(
                            Offset(center.x, center.y - g),
                            Offset(center.x + g * 0.75f, center.y),
                            Offset(center.x, center.y + g),
                            Offset(center.x - g * 0.75f, center.y)
                        )
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            lineTo(pts[1].x, pts[1].y)
                            lineTo(pts[2].x, pts[2].y)
                            lineTo(pts[3].x, pts[3].y)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                listOf(AmuletGold, AmuletEmerald)
                            )
                        )
                        // Inner emerald
                        val gi = g * 0.55f
                        val ipts = listOf(
                            Offset(center.x, center.y - gi),
                            Offset(center.x + gi * 0.75f, center.y),
                            Offset(center.x, center.y + gi),
                            Offset(center.x - gi * 0.75f, center.y)
                        )
                        val ipath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(ipts[0].x, ipts[0].y)
                            lineTo(ipts[1].x, ipts[1].y)
                            lineTo(ipts[2].x, ipts[2].y)
                            lineTo(ipts[3].x, ipts[3].y)
                            close()
                        }
                        drawPath(path = ipath, color = AmuletBg)
                    }

                    // Particle sparks
                    if (gemProgress > 0.5f) {
                        repeat(8) { i ->
                            val a = (i / 8f) * 2f * Math.PI.toFloat() + ringRotation * 0.01f
                            val r = baseR * 0.8f * (0.5f + gemProgress * 0.5f)
                            val x = center.x + cos(a) * r
                            val y = center.y + sin(a) * r
                            drawCircle(
                                color = AmuletGold.copy(alpha = 0.6f * gemProgress),
                                radius = 2.5f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── "SKB" text ──
            Text(
                text = "SKB",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = AmuletText.copy(alpha = skbAlpha),
                letterSpacing = 8.sp
            )

            // ── "Sakib" text ──
            Text(
                text = "SAKIB",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = AmuletEmerald.copy(alpha = sakibAlpha),
                letterSpacing = 12.sp
            )

            Spacer(Modifier.height(12.dp))

            // ── Tagline ──
            Text(
                text = "P R E M I U M   M U S I C",
                fontSize = 10.sp,
                color = AmuletTextMuted.copy(alpha = tagAlpha),
                letterSpacing = 4.sp
            )
        }
    }
}
