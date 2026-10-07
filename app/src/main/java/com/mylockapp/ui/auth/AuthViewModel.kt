package com.mylockapp.ui.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.mylockapp.audio.SfxPlayer
import com.mylockapp.di.AppContainer
import com.mylockapp.domain.model.AuthStep
import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.domain.model.UserProfile
import com.mylockapp.security.SecurityVerdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream

/**
 * Normal : BIOMETRIC -> PIN -> FACE -> GRANTED (voice + profile reveal) | DENIED
 * Enroll : REGISTER -> FACE -> GRANTED
 */
class AuthViewModel(
    private val c: AppContainer,
    val enrollMode: Boolean
) : ViewModel() {

    companion object {
        const val MAX_PIN_FAILS = 3
        private const val PHOTO_MAX_SIDE = 480
    }

    private val _step = MutableStateFlow(if (enrollMode) AuthStep.REGISTER else AuthStep.BIOMETRIC)
    val step: StateFlow<AuthStep> = _step.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _photo = MutableStateFlow<Bitmap?>(null)
    val photo: StateFlow<Bitmap?> = _photo.asStateFlow()

    private var pinFails = 0

    init {
        if (!enrollMode && c.securityManager.evaluate() is SecurityVerdict.Compromised) deny()
    }

    // Layer 1
    fun onBiometricSuccess() { if (_step.value == AuthStep.BIOMETRIC) _step.value = AuthStep.PIN }
    fun onBiometricFatal() = deny()

    // Layer 2 - returns true if accepted
    fun submitPin(pin: String): Boolean {
        if (_step.value != AuthStep.PIN) return false
        return if (c.verifyPin(pin)) {
            _step.value = AuthStep.FACE; true
        } else {
            if (++pinFails >= MAX_PIN_FAILS) deny()
            false
        }
    }

    // Enroll only: registration form
    fun onRegisterSubmit(profile: UserProfile) {
        if (_step.value != AuthStep.REGISTER) return
        c.profileRepository.saveUserProfile(profile)
        _profile.value = profile
        _step.value = AuthStep.FACE
    }

    // Layer 3
    fun onFaceVerified(sig: FaceSignature, snapshot: Bitmap?) {
        if (_step.value != AuthStep.FACE) return
        val photo = snapshot?.let { scaleDown(it) }

        if (enrollMode) {
            c.profileRepository.saveFace(sig)
            photo?.let { c.profileRepository.saveFacePhoto(it.toJpeg()) }
            _photo.value = photo
            grant()
        } else if (c.matchProfile(sig)) {
            _profile.value = c.profileRepository.loadUserProfile()
            _photo.value = photo ?: c.profileRepository.loadFacePhoto()
                ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            grant()
        } else {
            deny()
        }
    }

    fun retry() {
        pinFails = 0
        _profile.value = null
        _photo.value = null
        _step.value = AuthStep.BIOMETRIC
    }

    private fun grant() {
        c.alarm.stop()
        c.sfx.play(SfxPlayer.Clip.GRANTED)
        _step.value = AuthStep.GRANTED
    }

    private fun deny() {
        c.sfx.play(SfxPlayer.Clip.DENIED)
        c.alarm.start()
        _step.value = AuthStep.DENIED
    }

    private fun scaleDown(src: Bitmap): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= PHOTO_MAX_SIDE) return src
        val k = PHOTO_MAX_SIDE.toFloat() / longest
        return Bitmap.createScaledBitmap(src, (src.width * k).toInt(), (src.height * k).toInt(), true)
    }

    private fun Bitmap.toJpeg(): ByteArray {
        val out = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }
}
