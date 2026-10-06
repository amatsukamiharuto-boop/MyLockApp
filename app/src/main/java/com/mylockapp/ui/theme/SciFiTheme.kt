package com.mylockapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Hud {
    val Bg = Color(0xFF050A14)
    val Panel = Color(0xFF0B1F33)
    val Cyan = Color(0xFF00E5FF)
    val Magenta = Color(0xFFFF2E93)
    val Green = Color(0xFF39FF14)
    val Mono = FontFamily.Monospace
}

@Composable
fun SciFiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Hud.Cyan, background = Hud.Bg, surface = Hud.Panel,
            error = Hud.Magenta, onBackground = Hud.Cyan, onSurface = Hud.Cyan
        ),
        content = content
    )
}

/** Full-screen HUD frame with a title bar. */
@Composable
fun HudScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Hud.Bg).systemBarsPadding().padding(16.dp)) {
        Column(
            Modifier.fillMaxSize()
                .border(1.dp, Hud.Cyan.copy(alpha = 0.6f), CutCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Hud.Cyan, fontFamily = Hud.Mono, fontWeight = FontWeight.Bold,
                fontSize = 14.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
