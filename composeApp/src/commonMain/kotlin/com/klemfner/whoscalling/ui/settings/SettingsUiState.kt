package com.klemfner.whoscalling.ui.settings

data class SettingsUiState(
    val contactCount: Int = 0,
    val countryIso: String = "",
    val importResult: ImportResult? = null,
)
