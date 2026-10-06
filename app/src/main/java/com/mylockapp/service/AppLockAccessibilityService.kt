package com.mylockapp.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import com.mylockapp.MyLockApp
import com.mylockapp.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Detects foreground-app changes, instantly covers the screen with an overlay
 * (so protected content never flashes), then launches the 3-layer AuthActivity.
 */
class AppLockAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val container get() = (application as MyLockApp).container
    private val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    private var cover: View? = null
    private var authPending = false
    private var imePackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        refreshImePackages()
        scope.launch {
            LockBus.events.collect { event ->
                removeCover()
                if (event != LockEvent.AUTH_VISIBLE) authPending = false
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui" || pkg in imePackages) return

        if (pkg in container.profileRepository.lockedPackages()) {
            if (authPending || container.sessions.isUnlocked(pkg)) return
            authPending = true
            showCover()
            startAuth(pkg)
        } else {
            // Left the protected app -> require auth again next time.
            container.sessions.clear()
        }
    }

    private fun startAuth(target: String) {
        if (container.securityManager.evaluate() is com.mylockapp.security.SecurityVerdict.Compromised) {
            // v2: decide policy (block, wipe keys, alarm...). v1 stub never reaches here.
        }
        startActivity(
            AuthActivity.intent(this, target).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
        )
        // Safety net: never leave the cover stuck.
        scope.launch { delay(3000); removeCover() }
    }

    private fun showCover() {
        if (cover != null) return
        val type = if (Settings.canDrawOverlays(this))
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

        val view = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#050A14"))
            isClickable = true // swallow touches
            addView(TextView(context).apply {
                text = "// SECURING TARGET //"
                setTextColor(Color.parseColor("#00E5FF"))
                typeface = android.graphics.Typeface.MONOSPACE
                textSize = 16f
            }, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.OPAQUE
        )
        runCatching { wm.addView(view, lp); cover = view }
    }

    private fun removeCover() {
        cover?.let { runCatching { wm.removeView(it) } }
        cover = null
    }

    private fun refreshImePackages() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imePackages = imm.enabledInputMethodList.map { it.packageName }.toSet()
    }

    override fun onInterrupt() = removeCover()

    override fun onDestroy() {
        removeCover()
        scope.cancel()
        super.onDestroy()
    }
}
