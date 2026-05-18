package com.example.pruningapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D5A27),        // Głęboka, szlachetna zieleń
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E8D4), // Bardzo jasna szałwia
    onPrimaryContainer = Color(0xFF0C1F0A),
    secondary = Color(0xFF55624C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E7CB),
    onSecondaryContainer = Color(0xFF131F0E),
    background = Color(0xFFFDFDFB),      // Miękka biel (Sand/Soft White)
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFDFDFB),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF43493F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFAED581),
    onSecondary = Color(0xFF33691E),
    secondaryContainer = Color(0xFF558B2F),
    onSecondaryContainer = Color(0xFFDCEDC8),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2A3B2A),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

@Composable
fun PlantPruningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // dynamicColor wyłączone — zawsze używamy zielonej palety
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
