package com.hoshiyomix.complaintlogbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange100 = Color(0xFFFFE0B2)
private val Orange500 = Color(0xFFFF9800)
private val Orange600 = Color(0xFFFB8C00)
private val Orange700 = Color(0xFFF57C00)

private val LightColors = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange700,
    secondary = Orange600,
    onSecondary = Color.White,
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E),
    onError = Color.White,
    outline = Color(0xFFE0E0E0)
)

private val DarkColors = darkColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange700,
    onPrimaryContainer = Orange100,
    secondary = Orange600,
    onSecondary = Color.White,
    tertiary = Color(0xFF66BB6A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    outline = Color(0xFF424242)
)

@Composable
fun MelastiDreamTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
