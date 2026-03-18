package com.raf.doubletaptolock.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.raf.doubletaptolock.utility.Helper.checkAccessibilityPermission
import com.raf.doubletaptolock.utility.SettingsRepository
import com.raf.doubletaptolock.utility.TouchArea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UiState(
            isLandscapeEnabled = settingsRepo.isLandscapeEnabled,
            touchArea = try {
                TouchArea.valueOf(settingsRepo.touchArea)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid touch area value: ${settingsRepo.touchArea}", e)
                TouchArea.FULL
            },
            height = settingsRepo.height
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Permission
    fun checkPermission(context: Context) {
        val isEnabled = checkAccessibilityPermission(context)
        _uiState.update { it.copy(isServiceEnabled = isEnabled) }
    }

    // Landscape Settings
    fun toggleLandscape(enabled: Boolean) {
        settingsRepo.isLandscapeEnabled = enabled
        _uiState.update { it.copy(isLandscapeEnabled = enabled) }
    }

    // Touch Area Settings
    fun nextTouchArea() {
        val entries = TouchArea.entries
        val currentIndex = _uiState.value.touchArea.ordinal
        val nextIndex = (currentIndex + 1) % entries.size
        val nextArea = entries[nextIndex]

        settingsRepo.touchArea = nextArea.name
        _uiState.update { it.copy(touchArea = nextArea) }
    }

    fun previousTouchArea() {
        val entries = TouchArea.entries
        val currentIndex = _uiState.value.touchArea.ordinal
        val prevIndex = (currentIndex - 1 + entries.size) % entries.size
        val prevArea = entries[prevIndex]

        settingsRepo.touchArea = prevArea.name
        _uiState.update { it.copy(touchArea = prevArea) }
    }

    // Height Settings
    fun increaseHeight() {
        val newHeight = when (val currentHeight = _uiState.value.height) {
            0 -> {
                12
            }

            100 -> {
                0
            }

            else -> {
                currentHeight + 1
            }
        }

        settingsRepo.height = newHeight
        _uiState.update { it.copy(height = newHeight) }
    }

    fun decreaseHeight() {
        val newHeight = when (val currentHeight = _uiState.value.height) {
            12 -> {
                0
            }

            0 -> {
                100
            }

            else -> {
                currentHeight - 1
            }
        }

        settingsRepo.height = newHeight
        _uiState.update { it.copy(height = newHeight) }
    }

    fun resetHeight() {
        settingsRepo.height = 0
        _uiState.update { it.copy(height = 0) }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}