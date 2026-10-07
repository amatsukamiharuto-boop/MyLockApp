package com.mylockapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylockapp.domain.model.UserProfile
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen

@Composable
fun ProfileFormScreen(onSubmit: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var verified by remember { mutableStateOf(true) }

    val valid = name.isNotBlank() && id.isNotBlank()

    HudScreen("// REGISTRATION //") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HudField("NAME", name) { name = it }
            HudField("ID", id) { id = it }
            HudField("ROLE", role) { role = it }
            HudField("POSITION", position) { position = it }

            Text("STATUS", color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusButton("VERIFIED", selected = verified, color = Hud.Green, modifier = Modifier.weight(1f)) {
                    verified = true
                }
                StatusButton("UNVERIFIED", selected = !verified, color = Hud.Magenta, modifier = Modifier.weight(1f)) {
                    verified = false
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onSubmit(
                        UserProfile(
                            name = name.trim(),
                            id = id.trim(),
                            role = role.trim(),
                            position = position.trim(),
                            verified = verified
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Hud.Cyan, contentColor = Color.Black)
            ) {
                Text("SAVE PROFILE", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.15f),
            contentColor = if (selected) Color.Black else color
        )
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun HudField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Hud.Green,
            unfocusedTextColor = Hud.Green,
            focusedBorderColor = Hud.Cyan,
            unfocusedBorderColor = Hud.Cyan.copy(alpha = 0.4f),
            focusedLabelColor = Hud.Cyan,
            unfocusedLabelColor = Hud.Cyan.copy(alpha = 0.7f),
            cursorColor = Hud.Cyan
        )
    )
}
