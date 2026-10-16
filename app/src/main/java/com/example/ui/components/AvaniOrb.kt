package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DangerRuby
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SoftAmethyst
import com.example.voice.VoiceState

@Composable
fun AvaniOrb(
    voiceState: VoiceState,
    audioRms: Float,
    isEmergencyStopped: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow organic ambient breathing
    val ambientBreath by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_breath"
    )

    // Fast rotation for rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Wave ripple pulse
    val rippleScale = remember { Animatable(1f) }
    LaunchedEffect(audioRms, voiceState) {
        val targetScale = when (voiceState) {
            VoiceState.LISTENING -> 1.0f + (audioRms.coerceIn(0f, 10f) / 18f)
            VoiceState.SPEAKING -> 1.0f + (ambientBreath * 0.12f)
            VoiceState.PROCESSING -> 1.08f
            else -> 1.0f
        }
        rippleScale.animateTo(targetScale, tween(150))
    }

    // Color gradient based on state
    val coreColors = when {
        isEmergencyStopped -> listOf(DangerRuby, Color(0xFF7F1D1D))
        voiceState == VoiceState.LISTENING -> listOf(ElectricCyan, NeonViolet, GlowingAmber)
        voiceState == VoiceState.PROCESSING -> listOf(NeonViolet, SoftAmethyst, ElectricCyan)
        voiceState == VoiceState.SPEAKING -> listOf(SoftAmethyst, ElectricCyan, GlowingAmber)
        voiceState == VoiceState.ERROR -> listOf(DangerRuby, GlowingAmber)
        else -> listOf(NeonViolet, Color(0xFF4338CA), ElectricCyan)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Avani Assistant Interactive Orb" }
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        // Dynamic Canvas for Holographic Rings and Soundwave Aura
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = this.size.minDimension / 3.4f

            // Outer soundwave ring 1
            if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                drawCircle(
                    color = coreColors.first().copy(alpha = 0.22f),
                    radius = baseRadius * 1.55f * rippleScale.value,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Outer ring 2
                drawCircle(
                    color = coreColors.last().copy(alpha = 0.14f),
                    radius = baseRadius * 1.85f * rippleScale.value,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Orbital Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = coreColors.map { it.copy(alpha = 0.4f) }
                ),
                radius = baseRadius * 1.25f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Inner Glowing Sphere
        Box(
            modifier = Modifier
                .size(size * 0.62f)
                .scale(ambientBreath * rippleScale.value)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = coreColors
                    )
                )
        )

        // Center Brilliant Core Sparkle
        Box(
            modifier = Modifier
                .size(size * 0.22f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
    }
}
