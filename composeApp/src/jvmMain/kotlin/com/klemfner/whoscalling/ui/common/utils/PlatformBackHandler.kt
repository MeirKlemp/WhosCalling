package com.klemfner.whoscalling.ui.common.utils

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on JVM desktop
}
