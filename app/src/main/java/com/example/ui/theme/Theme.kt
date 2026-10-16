package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AvaniDarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = Color.White,
    primaryContainer = CosmicCardSurface,
    onPrimaryContainer = SoftAmethyst,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF1E3A44),
    onSecondaryContainer = ElectricCyan,
    tertiary = GlowingAmber,
    onTertiary = Color.Black,
    background = CosmicNavy,
    onBackground = TextPrimaryDark,
    surface = CosmicDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CosmicCardSurface,
    onSurfaceVariant = TextSecondaryDark,
    error = DangerRuby,
    onError = Color.White
)

private val AvaniLightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature Avani 2.O cosmic palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AvaniDarkColorScheme
        else -> AvaniDarkColorScheme // Voice assistant looks best in signature dark cosmic theme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
