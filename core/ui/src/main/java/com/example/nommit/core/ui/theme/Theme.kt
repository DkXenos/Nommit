package com.example.nommit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Nommit is single-theme by design: the comp defines one warm-cream zine and no
 * dark variant, and the style guide calls cream + ink "the constant frame". So
 * there is deliberately no dark colour scheme here -- shipping one would mean
 * inventing visuals the design doesn't answer (build spec §7.5).
 */
private val NommitColorScheme = lightColorScheme(
    primary = NommitColors.Chili,
    onPrimary = NommitColors.Cream,
    primaryContainer = NommitColors.Chili,
    onPrimaryContainer = NommitColors.Cream,

    secondary = NommitColors.Turmeric,
    onSecondary = NommitColors.Ink,
    secondaryContainer = NommitColors.Turmeric,
    onSecondaryContainer = NommitColors.Ink,

    tertiary = NommitColors.Pandan,
    onTertiary = NommitColors.Ink,
    tertiaryContainer = NommitColors.Pandan,
    onTertiaryContainer = NommitColors.Ink,

    background = NommitColors.Cream,
    onBackground = NommitColors.Ink,
    surface = NommitColors.Cream,
    onSurface = NommitColors.Ink,
    surfaceVariant = NommitColors.CardWhite,
    onSurfaceVariant = NommitColors.InkBody,

    outline = NommitColors.Ink,
    outlineVariant = NommitColors.Ink,
    scrim = NommitColors.Scrim,

    error = NommitColors.Chili,
    onError = NommitColors.Cream,
)

@Composable
fun NommitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NommitColorScheme,
        typography = NommitTypography,
        shapes = NommitShapes,
        content = content,
    )
}
