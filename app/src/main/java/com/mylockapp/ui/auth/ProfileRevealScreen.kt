package com.mylockapp.ui.auth

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylockapp.domain.model.UserProfile
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen
import kotlinx.coroutines.delay

private data class InfoRow(val label: String, val value: String, val color: Color)

@Composable
fun ProfileRevealScreen(profile: UserProfile?, photo: Bitmap?) {
    val rows = listOf(
        InfoRow("NAME", profile?.name ?: "UNKNOWN", Hud.Green),
        InfoRow("ID", profile?.id ?: "-", Hud.Green),
        InfoRow("ROLE", profile?.role?.ifBlank { "-" } ?: "-", Hud.Green),
        InfoRow("POSITION", profile?.position?.ifBlank { "-" } ?: "-", Hud.Green),
        InfoRow(
            "STATUS",
            if (profile?.verified == true) "VERIFIED" else "UNVERIFIED",
            if (profile?.verified == true) Hud.Green else Hud.Magenta
        )
    )

    var visibleCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(700)
        for (i in rows.indices) {
            visibleCount = i + 1
            delay(450)
        }
    }

    val t = rememberInfiniteTransition(label = "reveal")
    val scan by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "scan"
    )

    HudScreen("// ACCESS GRANTED //") {
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(140.dp)) {
                if (photo != null) {
                    Image(
                        bitmap = photo.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                    )
                }
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val c = 20.dp.toPx()
                    val s = 2.dp.toPx()
                    val col = Hud.Cyan
                    drawLine(col, Offset(0f, 0f), Offset(c, 0f), s)
                    drawLine(col, Offset(0f, 0f), Offset(0f, c), s)
                    drawLine(col, Offset(w, 0f), Offset(w - c, 0f), s)
                    drawLine(col, Offset(w, 0f), Offset(w, c), s)
                    drawLine(col, Offset(0f, h), Offset(c, h), s)
                    drawLine(col, Offset(0f, h), Offset(0f, h - c), s)
                    drawLine(col, Offset(w, h), Offset(w - c, h), s)
                    drawLine(col, Offset(w, h), Offset(w, h - c), s)
                    val y = h * scan
                    drawLine(Hud.Green.copy(alpha = 0.8f), Offset(0f, y), Offset(w, y), s)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                rows.forEachIndexed { i, row ->
                    AnimatedVisibility(
                        visible = i < visibleCount,
                        enter = fadeIn() + slideInHorizontally { it / 2 }
                    ) {
                        Column(Modifier.padding(bottom = 10.dp)) {
                            Text(row.label, color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 11.sp)
                            Text(row.value, color = row.color, fontFamily = Hud.Mono, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}
