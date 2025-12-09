package com.ejercicio.my_application_social.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

// --- AÑADIR: Colores para el tema Claro ---
// Estos colores se usarán en Login/Register
private val LightColorScheme = lightColorScheme(
    primary = Primary, // Puedes usar el mismo morado como primario
    secondary = Secondary,
    tertiary = Pink80,
    background = Color.White, // Fondo blanco
    surface = Color.White,    // Superficie blanca
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black, // ¡Importante! Texto sobre fondo será negro
    onSurface = Color.Black     // ¡Importante! Texto sobre superficie será negro
)
@Composable
fun PhotoFeedTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if(useDarkTheme){
        DarkColorScheme
    }else{
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}