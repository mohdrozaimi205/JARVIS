package com.mohdrozaimi.jarvis.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Tema "Bilik Kawalan Fajar": arang hangat + tembaga. */
object Fajar {
    val Bg = Color(0xFF16130F)
    val Panel = Color(0xFF211C17)
    val PanelHi = Color(0xFF2B241D)
    val Copper = Color(0xFFD9824B)
    val Ember = Color(0xFFF2B36B)
    val Cream = Color(0xFFF3E9DC)
    val Muted = Color(0xFFA89B8C)
    val Line = Color(0x33D9824B)
}

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Fajar.Copper,
            onPrimary = Fajar.Bg,
            secondary = Fajar.Ember,
            background = Fajar.Bg,
            onBackground = Fajar.Cream,
            surface = Fajar.Panel,
            onSurface = Fajar.Cream,
            surfaceVariant = Fajar.PanelHi,
            onSurfaceVariant = Fajar.Muted,
            outline = Fajar.Line,
        ),
        content = content,
    )
}
