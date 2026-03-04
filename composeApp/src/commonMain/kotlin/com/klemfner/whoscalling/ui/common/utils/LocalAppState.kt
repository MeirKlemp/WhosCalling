package com.klemfner.whoscalling.ui.common.utils

import androidx.compose.runtime.compositionLocalOf

val LocalIsExpanded = compositionLocalOf { false }

val LocalIsTouchMode = compositionLocalOf { true }

data class TouchModeState(
    val isTouchMode: Boolean,
    val onTouchModeChange: (Boolean) -> Unit,
)

val LocalTouchModeState = compositionLocalOf { TouchModeState(isTouchMode = true, onTouchModeChange = {}) }
