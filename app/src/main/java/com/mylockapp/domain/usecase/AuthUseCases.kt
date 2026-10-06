package com.mylockapp.domain.usecase

import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.domain.repository.ProfileRepository

class VerifyPinUseCase(private val repo: ProfileRepository) {
    operator fun invoke(pin: String): Boolean = repo.verifyPin(pin)
}

/** Layer 4 (post-face): compares the live face signature with the encrypted enrolled profile. */
class MatchProfileUseCase(private val repo: ProfileRepository) {
    companion object { const val THRESHOLD = 0.12f }

    operator fun invoke(live: FaceSignature?): Boolean {
        val enrolled = repo.loadFace() ?: return false
        return live != null && live.distanceTo(enrolled) <= THRESHOLD
    }
}
