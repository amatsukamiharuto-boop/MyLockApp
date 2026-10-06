package com.mylockapp.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen

@Composable
fun BiometricLayer(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFatal: () -> Unit,
    onAbort: () -> Unit
) {
    var attempt by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("PLACE FINGER ON SENSOR") }

    LaunchedEffect(attempt) {
        val canAuth = BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            message = "NO BIOMETRIC HARDWARE / NOT ENROLLED"
            onFatal()
            return@LaunchedEffect
        }
        val prompt = BiometricPrompt(
            activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) = onSuccess()
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    when (code) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onAbort()
                        else -> onFatal() // lockout, hw error, etc.
                    }
                }
                // onAuthenticationFailed: single bad read, prompt stays open - no action needed.
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("LAYER 01 // BIOMETRIC")
                .setSubtitle("Verify identity to continue")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("ABORT")
                .setConfirmationRequired(false)
                .build()
        )
    }

    HudScreen("LAYER 01 // BIOMETRIC SCAN") {
        Spacer(Modifier.weight(1f))
        Text(message, color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { attempt++ }) { Text("RESCAN") }
        Spacer(Modifier.weight(1f))
    }
}
