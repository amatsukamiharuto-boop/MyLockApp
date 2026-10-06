package com.mylockapp.ui.auth

import androidx.lifecycle.ViewModel
import com.mylockapp.audio.SfxPlayer
import com.mylockapp.di.AppContainer
import com.mylockapp.domain.model.AuthStep
import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.security.SecurityVerdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sequential state machine: BIOMETRIC -> PIN -> FACE -> MATCHING -> GRANTED | DENIED. */
class AuthViewModel(
    private val c: AppContainer,
    val enrollMode: Boolean
) : ViewModel() {

    companion object { const val MAX_PIN_FAILS = 3 }

    private val _step = MutableStateFlow(if (enrollMode) AuthStep.FACE else AuthStep.BIOMETRIC)
    val step: StateFlow<AuthStep> = _step.asStateFlow()

    private var pinFails = 0
    private var liveFace: FaceSignature? = null

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

    // Layer 3
    fun onFaceVerified(sig: FaceSignature) {
        if (_step.value != AuthStep.FACE) return
        if (enrollMode) {
            c.profileRepository.saveFace(sig)
            c.sfx.play(SfxPlayer.Clip.GRANTED)
            _step.value = AuthStep.GRANTED
        } else {
            liveFace = sig
            _step.value = AuthStep.MATCHING
        }
    }

    // Data-matching HUD finished
    fun onMatchingDone() {
        if (_step.value != AuthStep.MATCHING) return
        if (c.matchProfile(liveFace)) grant() else deny()
    }

    fun retry() {
        pinFails = 0; liveFace = null
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
}
