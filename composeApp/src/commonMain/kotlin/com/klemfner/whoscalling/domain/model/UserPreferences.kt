package com.klemfner.whoscalling.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val countryIso: String = "",
    val touchMode: Boolean = true,
    val routerIp: String = "",
    val refreshRateSeconds: Long = DEFAULT_REFRESH_RATE_SECONDS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
) {
    companion object {
        const val DEFAULT_REFRESH_RATE_SECONDS = 5L
    }
}
