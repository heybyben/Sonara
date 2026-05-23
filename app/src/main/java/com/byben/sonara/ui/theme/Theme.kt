package com.byben.sonara.ui.theme

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

private val SonaraLightColorScheme = lightColorScheme(
    primary = PurpleAccent,
    onPrimary = OnSurface,
    secondary = PinkAccent,
    onSecondary = OnSurface,
    tertiary = PurpleLight,
    background = Color(0xFFF4F0FF),
    onBackground = OnSurface,
    surface = Color(0xFFF0EBF9),
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE7DFF5),
    onSurfaceVariant = OnSurfaceMuted,
    primaryContainer = Color(0xFFE8D9FF),
    onPrimaryContainer = OnSurface,
)

@Composable
fun SonaraTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isSystemInDarkTheme() -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        isSystemInDarkTheme() -> SonaraDarkColorScheme
        else -> SonaraLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SonaraTypography,
        content = content
    )
}
