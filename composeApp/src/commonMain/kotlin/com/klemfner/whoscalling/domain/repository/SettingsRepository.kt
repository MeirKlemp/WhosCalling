package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.ThemeMode
import com.klemfner.whoscalling.domain.model.UserPreferences
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val preferences: StateFlow<UserPreferences>
    val currentCountryIso: String
    val currentTouchMode: Boolean
    val currentRouterIp: String
    val currentRefreshRateSeconds: Long
    val currentRefreshOnStartup: Boolean
    val currentThemeMode: ThemeMode
    suspend fun setCountryIso(iso: String)
    suspend fun setTouchMode(touchMode: Boolean)
    suspend fun setRouterIp(ip: String)
    suspend fun setRefreshRateSeconds(seconds: Long)
    suspend fun setRefreshOnStartup(refreshOnStartup: Boolean)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun resetToDefault()
}
