package com.mylockapp.security

import android.content.Context

/**
 * Single seam for runtime app self-protection (RASP).
 *
 * UI, ViewModels and the lock service only depend on this interface. In v2, implement
 * `RaspSecurityManager` (root detection, Frida/Xposed anti-hooking, debugger/emulator checks,
 * signature/tamper checks, e.g. via freeRASP or Play Integrity) and swap it in
 * AppContainer. No UI code changes required.
 */
sealed interface SecurityVerdict {
    data object Secure : SecurityVerdict
    data class Compromised(val reasons: List<String>) : SecurityVerdict
}

interface SecurityManager {
    /** Called before each auth session starts. Cheap, synchronous. */
    fun evaluate(): SecurityVerdict

    /** Hook for continuous monitoring (v2). */
    fun startMonitoring(onThreat: (SecurityVerdict.Compromised) -> Unit) {}
    fun stopMonitoring() {}
}

/** v1 stub: always secure. */
class DefaultSecurityManager(@Suppress("unused") private val context: Context) : SecurityManager {
    override fun evaluate(): SecurityVerdict {
        // TODO(v2): root check, hook detection, debugger/tamper checks.
        return SecurityVerdict.Secure
    }
}
