package com.example.pruningapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BotanicalGreen,
    onPrimary = BotanicalOnPrimary,
    primaryContainer = BotanicalGreenContainer,
    onPrimaryContainer = BotanicalOnPrimaryContainer,
    secondary = BotanicalSecondary,
    onSecondary = BotanicalOnSecondary,
    secondaryContainer = BotanicalSecondaryContainer,
    onSecondaryContainer = BotanicalOnSecondaryContainer,
    tertiary = BotanicalTertiary,
    onTertiary = BotanicalOnTertiary,
    tertiaryContainer = BotanicalTertiaryContainer,
    onTertiaryContainer = BotanicalOnTertiaryContainer,
    background = BotanicalBackground,
    onBackground = BotanicalOnBackground,
    surface = BotanicalSurface,
    onSurface = BotanicalOnSurface,
    surfaceVariant = BotanicalSurfaceVariant,
    onSurfaceVariant = BotanicalOnSurfaceVariant,
    outline = BotanicalOutline,
    outlineVariant = BotanicalOutlineVariant,
    error = BotanicalError,
    onError = BotanicalOnError,
    errorContainer = BotanicalErrorContainer,
    onErrorContainer = BotanicalOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = BotanicalGreenDark,
    onPrimary = BotanicalOnPrimaryDark,
    primaryContainer = BotanicalGreenContainerDark,
    onPrimaryContainer = BotanicalOnPrimaryContainerDark,
    secondary = BotanicalSecondaryDark,
    onSecondary = BotanicalOnSecondaryDark,
    secondaryContainer = BotanicalSecondaryContainerDark,
    onSecondaryContainer = BotanicalOnSecondaryContainerDark,
    tertiary = BotanicalTertiaryDark,
    onTertiary = BotanicalOnTertiaryDark,
    tertiaryContainer = BotanicalTertiaryContainerDark,
    onTertiaryContainer = BotanicalOnTertiaryContainerDark,
    background = BotanicalBackgroundDark,
    onBackground = BotanicalOnBackgroundDark,
    surface = BotanicalSurfaceDark,
    onSurface = BotanicalOnSurfaceDark,
    surfaceVariant = BotanicalSurfaceVariantDark,
    onSurfaceVariant = BotanicalOnSurfaceVariantDark,
    outline = BotanicalOutlineDark,
    outlineVariant = BotanicalOutlineVariantDark
)

@Composable
fun PlantPruningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
