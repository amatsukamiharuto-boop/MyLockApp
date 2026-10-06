package com.mylockapp.ui.auth

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.mylockapp.domain.model.FaceSignature
import kotlin.math.abs
import kotlin.math.hypot

sealed interface FaceReading {
    data object NoFace : FaceReading
    data object MultipleFaces : FaceReading
    data class Poor(val hint: String) : FaceReading
    data class Good(val signature: FaceSignature) : FaceReading
}

class FaceAnalyzer(private val onReading: (FaceReading) -> Unit) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(proxy: ImageProxy) {
        val media = proxy.image
        if (media == null) { proxy.close(); return }
        val input = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
        detector.process(input)
            .addOnSuccessListener { faces ->
                onReading(
                    when {
                        faces.isEmpty() -> FaceReading.NoFace
                        faces.size > 1 -> FaceReading.MultipleFaces
                        else -> evaluate(faces[0])
                    }
                )
            }
            .addOnCompleteListener { proxy.close() }
    }

    private fun evaluate(f: Face): FaceReading {
        if (abs(f.headEulerAngleY) > 12f || abs(f.headEulerAngleZ) > 12f)
            return FaceReading.Poor("LOOK STRAIGHT AT CAMERA")
        if ((f.leftEyeOpenProbability ?: 0f) < 0.5f || (f.rightEyeOpenProbability ?: 0f) < 0.5f)
            return FaceReading.Poor("KEEP EYES OPEN")

        fun p(t: Int) = f.getLandmark(t)?.position
        val le = p(FaceLandmark.LEFT_EYE); val re = p(FaceLandmark.RIGHT_EYE)
        val nose = p(FaceLandmark.NOSE_BASE)
        val ml = p(FaceLandmark.MOUTH_LEFT); val mr = p(FaceLandmark.MOUTH_RIGHT)
        val mb = p(FaceLandmark.MOUTH_BOTTOM)
        if (le == null || re == null || nose == null || ml == null || mr == null || mb == null)
            return FaceReading.Poor("FACE NOT FULLY VISIBLE")

        fun d(ax: Float, ay: Float, bx: Float, by: Float) = hypot(ax - bx, ay - by)
        val eye = d(le.x, le.y, re.x, re.y)
        if (eye < 1f) return FaceReading.Poor("MOVE CLOSER")
        val mx = (le.x + re.x) / 2f; val my = (le.y + re.y) / 2f

        return FaceReading.Good(
            FaceSignature(
                floatArrayOf(
                    d(nose.x, nose.y, mx, my) / eye,
                    d(ml.x, ml.y, mr.x, mr.y) / eye,
                    d(mb.x, mb.y, nose.x, nose.y) / eye,
                    d(ml.x, ml.y, le.x, le.y) / eye,
                    d(mr.x, mr.y, re.x, re.y) / eye
                )
            )
        )
    }

    fun close() = detector.close()
}
