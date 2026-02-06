package com.it2161.s231292a.movieviewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Netflix-inspired dark color scheme with blue accents
private val NetflixDarkColorScheme = darkColorScheme(
    primary = NetflixBlue,
    onPrimary = Color.White,
    primaryContainer = NetflixDarkGray,
    onPrimaryContainer = Color.White,
    secondary = NetflixBlueLight,
    onSecondary = Color.White,
    secondaryContainer = NetflixMediumGray,
    onSecondaryContainer = Color.White,
    tertiary = NetflixBlueMuted,
    onTertiary = Color.White,
    tertiaryContainer = NetflixLightGray,
    onTertiaryContainer = Color.White,
    background = NetflixBlack,
    onBackground = Color.White,
    surface = NetflixDarkGray,
    onSurface = Color.White,
    surfaceVariant = NetflixMediumGray,
    onSurfaceVariant = NetflixTextGray,
    surfaceTint = NetflixBlue,
    inverseSurface = Color.White,
    inverseOnSurface = NetflixBlack,
    error = NetflixRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = NetflixLightGray,
    outlineVariant = NetflixMediumGray,
    scrim = NetflixBlack
)

// Light color scheme with Netflix blue (for users who prefer light mode)
private val NetflixLightColorScheme = lightColorScheme(
    primary = NetflixBlueDark,
    onPrimary = Color.White,
    primaryContainer = NetflixBlueLight,
    onPrimaryContainer = Color.White,
    secondary = NetflixBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = NetflixBlueDark,
    tertiary = NetflixBlueMuted,
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = NetflixBlack,
    surface = Color.White,
    onSurface = NetflixBlack,
    surfaceVariant = Color(0xFFE7E7E7),
    onSurfaceVariant = Color(0xFF5C5C5C),
    error = NetflixRed,
    onError = Color.White,
    outline = Color(0xFFCCCCCC)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NetflixDarkColorScheme else NetflixLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
