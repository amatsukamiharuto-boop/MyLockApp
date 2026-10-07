package com.mylockapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mylockapp.MyLockApp
import com.mylockapp.domain.model.AuthStep
import com.mylockapp.service.LockBus
import com.mylockapp.service.LockEvent
import com.mylockapp.ui.main.MainActivity
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen
import com.mylockapp.ui.theme.SciFiTheme
import kotlinx.coroutines.delay

class AuthActivity : FragmentActivity() {

    companion object {
        private const val EXTRA_TARGET = "target"
        private const val EXTRA_ENROLL = "enroll"
        private const val REVEAL_MILLIS = 5000L

        fun intent(ctx: Context, target: String?) =
            Intent(ctx, AuthActivity::class.java).putExtra(EXTRA_TARGET, target)
        fun enrollIntent(ctx: Context) =
            Intent(ctx, AuthActivity::class.java).putExtra(EXTRA_ENROLL, true)
    }

    private var finishedOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // no screenshots / recents preview

        val container = (application as MyLockApp).container
        val target = intent.getStringExtra(EXTRA_TARGET)
        val enroll = intent.getBooleanExtra(EXTRA_ENROLL, false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = abort()
        })

        setContent {
            SciFiTheme {
                val vm: AuthViewModel = viewModel(
                    factory = viewModelFactory { initializer { AuthViewModel(container, enroll) } }
                )
                val step by vm.step.collectAsState()
                val profile by vm.profile.collectAsState()
                val photo by vm.photo.collectAsState()

                LaunchedEffect(step) {
                    if (step == AuthStep.GRANTED) {
                        delay(REVEAL_MILLIS) // let the voice + profile reveal play out
                        onGranted(target, container.sessions::grant)
                    }
                }

                when (step) {
                    AuthStep.BIOMETRIC -> BiometricLayer(
                        activity = this@AuthActivity,
                        onSuccess = vm::onBiometricSuccess,
                        onFatal = vm::onBiometricFatal,
                        onAbort = ::abort
                    )
                    AuthStep.PIN -> PinScreen(
                        title = "LAYER 02 // ACCESS CODE",
                        onSubmit = vm::submitPin
                    )
                    AuthStep.REGISTER -> ProfileFormScreen(onSubmit = vm::onRegisterSubmit)
                    AuthStep.FACE -> FaceHudScreen(onVerified = vm::onFaceVerified)
                    AuthStep.MATCHING -> Unit // no longer used
                    AuthStep.GRANTED -> ProfileRevealScreen(profile, photo)
                    AuthStep.DENIED -> StatusScreen("ACCESS DENIED", Hud.Magenta, onRetry = vm::retry)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LockBus.emit(LockEvent.AUTH_VISIBLE) // service drops its cover overlay
    }

    private fun onGranted(target: String?, grant: (String) -> Unit) {
        finishedOk = true
        if (target != null) {
            grant(target)
            if (target == packageName) startActivity(Intent(this, MainActivity::class.java))
        }
        LockBus.emit(LockEvent.UNLOCKED)
        finish() // target app is already behind us and becomes visible
    }

    private fun abort() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        LockBus.emit(LockEvent.ABORTED)
        finish()
    }

    override fun onStop() {
        super.onStop()
        // Leaving (e.g. Home/Recents) without success must reset the service state.
        if (!finishedOk && !isFinishing) LockBus.emit(LockEvent.ABORTED)
    }
}

@Composable
private fun StatusScreen(text: String, color: Color, onRetry: (() -> Unit)? = null) {
    HudScreen("// SYSTEM STATUS //") {
        Spacer(Modifier.weight(1f))
        Text(text, color = color, fontFamily = Hud.Mono, fontSize = 30.sp)
        Spacer(Modifier.height(24.dp))
        onRetry?.let { Button(onClick = it) { Text("RE-INITIALISE") } }
        Spacer(Modifier.weight(1f))
    }
}
