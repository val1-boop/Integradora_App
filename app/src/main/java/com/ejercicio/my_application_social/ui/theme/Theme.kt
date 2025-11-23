package com.ejercicio.my_application_social.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores minimalistas oscuros/morados
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Primary = Color(0xFFBB86FC)
val Secondary = Color(0xFF03DAC6)
val Background = Color(0xFF121212)
val Surface = Color(0xFF1E1E1E)
val OnPrimary = Color.Black
val OnBackground = Color.White
val OnSurface = Color.White

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Pink80,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface
)

@Composable
fun PhotoFeedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Forzamos Dark Theme por diseño
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}