package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF03141F),
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = IndigoGlow,
    onSecondary = Color(0xFF1E1E4F),
    secondaryContainer = Color(0xFF2E3166),
    onSecondaryContainer = Color(0xFFE0E0FF),
    tertiary = EmeraldGlow,
    onTertiary = Color(0xFF003822),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = RoseGlow,
    onError = Color.White
)

@Composable
fun VolumeMasterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
