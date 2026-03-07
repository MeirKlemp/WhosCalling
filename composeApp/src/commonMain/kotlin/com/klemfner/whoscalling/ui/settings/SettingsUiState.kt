package com.klemfner.whoscalling.ui.settings

data class SettingsUiState(
    val contactCount: Int = 0,
    val countryIso: String = "",
    val touchMode: Boolean = true,
    val routerIp: String = "",
    val importResult: ImportResult? = null,
)
