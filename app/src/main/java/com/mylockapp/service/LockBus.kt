package com.mylockapp.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Collections

enum class LockEvent { AUTH_VISIBLE, UNLOCKED, ABORTED }

/** In-process channel: AuthActivity -> AppLockAccessibilityService. */
object LockBus {
    private val _events = MutableSharedFlow<LockEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()
    fun emit(e: LockEvent) { _events.tryEmit(e) }
}

/** Packages the user has just unlocked. Cleared when the user switches to a non-locked app. */
class UnlockSessions {
    private val unlocked: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())
    fun grant(pkg: String) { unlocked += pkg }
    fun isUnlocked(pkg: String) = pkg in unlocked
    fun clear() = unlocked.clear()
}
