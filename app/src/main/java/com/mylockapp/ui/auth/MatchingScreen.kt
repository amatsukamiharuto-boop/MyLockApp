package com.mylockapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.mylockapp.R
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen
import kotlinx.coroutines.delay

/** Data-matching HUD. Replace res/raw/data_match.json with your own Lottie file. */
@Composable
fun MatchingScreen(onDone: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.data_match))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    LaunchedEffect(Unit) {
        delay(2800) // animation runs while profile validation completes
        onDone()
    }

    HudScreen("// DATA MATCHING //") {
        Spacer(Modifier.weight(1f))
        LottieAnimation(composition, { progress }, modifier = Modifier.size(260.dp))
        Spacer(Modifier.height(16.dp))
        Text("VALIDATING PROFILE SIGNATURE...", color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
    }
}
