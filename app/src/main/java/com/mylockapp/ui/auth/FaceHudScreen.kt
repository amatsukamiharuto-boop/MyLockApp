package com.mylockapp.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.ui.theme.Hud
import com.mylockapp.ui.theme.HudScreen
import java.util.concurrent.Executors

private const val REQUIRED_GOOD_FRAMES = 10

@Composable
fun FaceHudScreen(onVerified: (FaceSignature) -> Unit) {
    val ctx = LocalContext.current
    var hasCamera by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    LaunchedEffect(Unit) { if (!hasCamera) launcher.launch(Manifest.permission.CAMERA) }

    HudScreen("LAYER 03 // FACIAL RECOGNITION") {
        if (!hasCamera) {
            Spacer(Modifier.weight(1f))
            Text("CAMERA PERMISSION REQUIRED", color = Hud.Magenta, fontFamily = Hud.Mono, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
        } else {
            FaceScanner(onVerified)
        }
    }
}

@Composable
private fun ColumnScope.FaceScanner(onVerified: (FaceSignature) -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var hint by remember { mutableStateOf("ALIGN FACE WITHIN RETICLE") }
    var good by remember { mutableIntStateOf(0) }
    val samples = remember { mutableListOf<FaceSignature>() }
    var done by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    DisposableEffect(owner) {
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = FaceAnalyzer { reading ->
            if (done) return@FaceAnalyzer
            when (reading) {
                FaceReading.NoFace -> { samples.clear(); good = 0; hint = "NO FACE DETECTED" }
                FaceReading.MultipleFaces -> { samples.clear(); good = 0; hint = "SINGLE SUBJECT ONLY" }
                is FaceReading.Poor -> { samples.clear(); good = 0; hint = reading.hint }
                is FaceReading.Good -> {
                    samples += reading.signature
                    good = samples.size
                    hint = "HOLD STEADY..."
                    if (samples.size >= REQUIRED_GOOD_FRAMES) {
                        done = true
                        val n = samples.first().values.size
                        val avg = FloatArray(n) { i -> samples.map { it.values[i] }.average().toFloat() }
                        onVerified(FaceSignature(avg))
                    }
                }
            }
        }
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { it.setAnalyzer(executor, analyzer) }
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(ctx))

        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            executor.shutdown()
            analyzer.close()
        }
    }

    val t = rememberInfiniteTransition(label = "hud")
    val sweep by t.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "sweep")
    val scan by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "scan")

    Spacer(Modifier.weight(1f))
    Box(Modifier.size(320.dp), contentAlignment = Alignment.Center) {
        AndroidView(factory = { previewView }, modifier = Modifier.size(260.dp).clip(CircleShape))
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 6.dp.toPx()
            drawCircle(Hud.Cyan.copy(alpha = 0.5f), r, style = Stroke(2.dp.toPx()))
            drawArc(Hud.Cyan, sweep, 70f, false, Offset(center.x - r, center.y - r), Size(2 * r, 2 * r),
                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Hud.Magenta, -sweep, 40f, false, Offset(center.x - r, center.y - r), Size(2 * r, 2 * r),
                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            val y = center.y - 130.dp.toPx() + 260.dp.toPx() * scan
            drawLine(Hud.Green.copy(alpha = 0.8f), Offset(center.x - 130.dp.toPx(), y),
                Offset(center.x + 130.dp.toPx(), y), 2.dp.toPx())
        }
    }
    Spacer(Modifier.height(20.dp))
    Text(hint, color = Hud.Cyan, fontFamily = Hud.Mono, fontSize = 13.sp)
    Spacer(Modifier.height(12.dp))
    LinearProgressIndicator(
        progress = { good / REQUIRED_GOOD_FRAMES.toFloat() },
        color = Hud.Green, trackColor = Hud.Panel, modifier = Modifier.fillMaxWidth(0.7f)
    )
    Spacer(Modifier.weight(1f))
}
