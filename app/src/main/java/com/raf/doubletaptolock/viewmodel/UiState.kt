package com.raf.doubletaptolock.viewmodel

import com.raf.doubletaptolock.utility.TouchArea

data class UiState(
    val isServiceEnabled: Boolean = false,
    val isLandscapeEnabled: Boolean = true,
    val touchArea: TouchArea = TouchArea.FULL,
    val height: Int = 0
)
