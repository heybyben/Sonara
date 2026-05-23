package com.byben.sonara.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SonaraDarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = OnSurface,
    secondary = PinkAccent,
    onSecondary = OnSurface,
    tertiary = PurpleLight,
    background = PurpleDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = OnSurfaceMuted,
    primaryContainer = PurpleMid,
    onPrimaryContainer = OnSurface,
)

@Composable
fun SonaraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SonaraDarkColorScheme,
        typography = SonaraTypography,
        content = content
    )
}
