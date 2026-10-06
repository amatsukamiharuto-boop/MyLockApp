package com.mylockapp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen

const val PIN_LENGTH = 6

/** Reusable keypad. onSubmit returns true when accepted. */
@Composable
fun PinScreen(title: String, onSubmit: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun press(d: String) {
        if (pin.length >= PIN_LENGTH) return
        error = false
        pin += d
        if (pin.length == PIN_LENGTH) {
            if (!onSubmit(pin)) { error = true; pin = "" }
        }
    }

    HudScreen(title) {
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(PIN_LENGTH) { i ->
                Box(
                    Modifier.size(16.dp).clip(CircleShape)
                        .background(if (i < pin.length) Hud.Cyan else Hud.Panel)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (error) "CODE REJECTED" else " ",
            color = Hud.Magenta, fontFamily = Hud.Mono, fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))

        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "<"))
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) Spacer(Modifier.size(76.dp))
                    else OutlinedButton(
                        onClick = { if (key == "<") pin = pin.dropLast(1) else press(key) },
                        modifier = Modifier.size(76.dp).padding(vertical = 4.dp),
                        shape = CutCornerShape(12.dp),
                        border = BorderStroke(1.dp, Hud.Cyan.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(key, color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 22.sp) }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
