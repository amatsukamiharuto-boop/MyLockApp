package com.mylockapp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylockapp.MyLockApp
import com.mylockapp.ui.auth.AuthActivity
import com.mylockapp.ui.auth.PIN_LENGTH
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen
import com.mylockapp.ui.theme.SciFiTheme

/** Setup / settings. Gated behind the same 3-layer auth once a PIN exists. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val c = (application as MyLockApp).container

        if (c.profileRepository.hasPin() && c.profileRepository.loadFace() != null &&
            !c.sessions.isUnlocked(packageName)
        ) {
            startActivity(AuthActivity.intent(this, packageName))
            finish()
            return
        }

        setContent {
            SciFiTheme {
                var pin by remember { mutableStateOf("") }
                var pkg by remember { mutableStateOf("") }
                var locked by remember { mutableStateOf(c.profileRepository.lockedPackages()) }
                var status by remember { mutableStateOf("") }

                HudScreen("MYLOCKAPP // CONTROL") {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                            Text("1. ENABLE ACCESSIBILITY SERVICE")
                        }
                        Button(onClick = {
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                        }) { Text("2. GRANT OVERLAY PERMISSION") }

                        OutlinedTextField(
                            value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(PIN_LENGTH) },
                            label = { Text("SET $PIN_LENGTH-DIGIT PIN") }, singleLine = true
                        )
                        Button(enabled = pin.length == PIN_LENGTH, onClick = {
                            c.profileRepository.savePin(pin); pin = ""; status = "PIN ENCRYPTED & STORED"
                        }) { Text("3. SAVE PIN") }

                        Button(onClick = { startActivity(AuthActivity.enrollIntent(this@MainActivity)) }) {
                            Text("4. ENROLL FACE")
                        }

                        OutlinedTextField(
                            value = pkg, onValueChange = { pkg = it.trim() },
                            label = { Text("PACKAGE TO LOCK (e.g. com.whatsapp)") }, singleLine = true
                        )
                        Button(enabled = pkg.isNotBlank(), onClick = {
                            locked = locked + pkg
                            c.profileRepository.setLockedPackages(locked); pkg = ""
                        }) { Text("5. ADD LOCKED APP") }

                        locked.forEach { p ->
                            Button(onClick = {
                                locked = locked - p; c.profileRepository.setLockedPackages(locked)
                            }) { Text("UNLOCK  $p", fontSize = 12.sp) }
                        }
                        if (status.isNotEmpty()) Text(status, color = Hud.Green, fontFamily = Hud.Mono)
                    }
                }
            }
        }
    }
}
