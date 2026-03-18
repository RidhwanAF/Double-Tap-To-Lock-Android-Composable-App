package com.raf.doubletaptolock.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var isLandscapeEnabled: Boolean
        get() = prefs.getBoolean(KEY_LANDSCAPE, true)
        set(value) {
            prefs.edit { putBoolean(KEY_LANDSCAPE, value) }
        }

    var touchArea: String
        get() = prefs.getString(KEY_TOUCH_AREA, TouchArea.FULL.name)
            ?: TouchArea.FULL.name
        set(value) {
            prefs.edit { putString(KEY_TOUCH_AREA, value) }
        }

    var height: Int
        get() = prefs.getInt(KEY_HEIGHT, 0)
        set(value) {
            prefs.edit { putInt(KEY_HEIGHT, value) }
        }

    companion object {
        const val SHARED_PREFS_NAME = "StatusBarLockPrefs"

        const val KEY_LANDSCAPE = "enable_landscape"
        const val KEY_TOUCH_AREA = "touch_area"
        const val KEY_HEIGHT = "height"
    }
}