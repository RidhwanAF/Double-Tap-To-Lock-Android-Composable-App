package com.raf.doubletaptolock.service

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.raf.doubletaptolock.utility.SettingsRepository
import com.raf.doubletaptolock.utility.SettingsRepository.Companion.KEY_HEIGHT
import com.raf.doubletaptolock.utility.SettingsRepository.Companion.KEY_LANDSCAPE
import com.raf.doubletaptolock.utility.SettingsRepository.Companion.KEY_TOUCH_AREA
import com.raf.doubletaptolock.utility.TouchArea
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class StatusBarLockService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var layoutParams: WindowManager.LayoutParams

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LANDSCAPE || key == KEY_TOUCH_AREA || key == KEY_HEIGHT) {
                updateOverlayGeometry()
                flashTriggerFlow.value += 1
            }
        }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val flashTriggerFlow = MutableStateFlow(0)

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)

        val prefs = applicationContext.getSharedPreferences(
            SettingsRepository.SHARED_PREFS_NAME,
            MODE_PRIVATE
        )
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StatusBarLockService)
            setViewTreeSavedStateRegistryOwner(this@StatusBarLockService)

            setContent {

                val flashTrigger by flashTriggerFlow.collectAsState()

                // Animate when Settings Changes
                val backgroundColor = remember { Animatable(Color.Transparent) }
                LaunchedEffect(flashTrigger) {
                    if (flashTrigger > 0) {
                        backgroundColor.snapTo(Color.Red.copy(alpha = 0.5f))
                        delay(1000)
                        backgroundColor.animateTo(
                            targetValue = Color.Transparent,
                            animationSpec = tween(durationMillis = 500)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.value)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                                }
                            )
                        }
                )
            }
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            getSystemStatusBarHeightPx(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        windowManager.addView(composeView, layoutParams)
        updateOverlayGeometry()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOverlayGeometry()
    }

    private fun getSystemStatusBarHeightPx(): Int {
        if (!::windowManager.isInitialized) return (32 * resources.displayMetrics.density).toInt()

        val windowMetrics = windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
        val heightPx = insets.top

        return if (heightPx == 0) {
            (32 * resources.displayMetrics.density).toInt()
        } else {
            heightPx
        }
    }

    private fun updateOverlayGeometry() {
        if (!::windowManager.isInitialized || !::composeView.isInitialized || !::layoutParams.isInitialized) return

        val customHeightDp = settingsRepo.height
        if (customHeightDp > 0) {
            layoutParams.height = (customHeightDp * resources.displayMetrics.density).toInt()
        } else {
            layoutParams.height = getSystemStatusBarHeightPx()
        }

        val touchArea = try {
            TouchArea.valueOf(settingsRepo.touchArea)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TouchArea.FULL
        }

        val screenWidth = windowManager.currentWindowMetrics.bounds.width()

        when (touchArea) {
            TouchArea.FULL -> {
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }

            TouchArea.LEFT -> {
                layoutParams.width = screenWidth / 2
                layoutParams.gravity = Gravity.TOP or Gravity.START
            }

            TouchArea.RIGHT -> {
                layoutParams.width = screenWidth / 2
                layoutParams.gravity = Gravity.TOP or Gravity.END
            }
        }

        val orientation = resources.configuration.orientation
        val landscapeEnabled = settingsRepo.isLandscapeEnabled

        if (orientation == Configuration.ORIENTATION_LANDSCAPE && !landscapeEnabled) {
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            layoutParams.flags =
                layoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }

        windowManager.updateViewLayout(composeView, layoutParams)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        val prefs = applicationContext.getSharedPreferences(
            SettingsRepository.SHARED_PREFS_NAME,
            MODE_PRIVATE
        )
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)

        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }
}