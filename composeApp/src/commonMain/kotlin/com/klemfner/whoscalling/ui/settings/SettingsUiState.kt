package com.klemfner.whoscalling.ui.settings

import com.klemfner.whoscalling.domain.model.ThemeMode
import com.klemfner.whoscalling.domain.model.UserPreferences

data class SettingsUiState(
    val contactCount: Int = 0,
    val spamCount: Int = 0,
    val countryIso: String = "",
    val touchMode: Boolean = true,
    val routerIp: String = "",
    val refreshRateSeconds: Long = UserPreferences.DEFAULT_REFRESH_RATE_SECONDS,
    val refreshOnStartup: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val importResult: ImportResult? = null,
)
