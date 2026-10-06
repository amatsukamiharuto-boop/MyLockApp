package com.mylockapp.domain.model

import kotlin.math.sqrt

enum class AuthStep { BIOMETRIC, PIN, FACE, MATCHING, GRANTED, DENIED }

/**
 * Geometric face signature built from ML Kit landmarks (ratios normalised by eye distance).
 * NOTE: ML Kit only DETECTS faces; it does not identify people. This signature is a
 * lightweight placeholder. For real identity matching, replace with a TFLite embedding
 * model (e.g. MobileFaceNet) and compare cosine distance.
 */
class FaceSignature(val values: FloatArray) {
    fun distanceTo(other: FaceSignature): Float {
        if (values.size != other.values.size) return Float.MAX_VALUE
        var sum = 0f
        for (i in values.indices) { val d = values[i] - other.values[i]; sum += d * d }
        return sqrt(sum)
    }

    fun serialize(): String = values.joinToString(",")

    companion object {
        fun parse(s: String): FaceSignature? = runCatching {
            FaceSignature(s.split(",").map { it.toFloat() }.toFloatArray())
        }.getOrNull()
    }
}
