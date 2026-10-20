package com.limpio.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeleteRed = Color(0xFFE0524B)
val KeepGreen = Color(0xFF2E9E6B)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1F7A63),
    background = Color(0xFFF5F7F4),
    surface = Color(0xFFF5F7F4),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF6FD3B4),
    onPrimary = Color(0xFF00382B),
    background = Color(0xFF101412),
    surface = Color(0xFF101412),
)

@Composable
fun LimpioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        content = content,
    )
}
