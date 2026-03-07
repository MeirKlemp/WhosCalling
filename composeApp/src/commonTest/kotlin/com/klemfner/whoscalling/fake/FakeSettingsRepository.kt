package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(
    initialPreferences: UserPreferences = UserPreferences(countryIso = "US", touchMode = true),
    private val defaultPreferences: UserPreferences = initialPreferences,
) : SettingsRepository {

    private val _preferences = MutableStateFlow(initialPreferences)
    override val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    override val currentCountryIso: String
        get() = _preferences.value.countryIso

    override val currentTouchMode: Boolean
        get() = _preferences.value.touchMode

    override val currentRouterIp: String
        get() = _preferences.value.routerIp

    override val currentRefreshRateSeconds: Long
        get() = _preferences.value.refreshRateSeconds

    override suspend fun setCountryIso(iso: String) {
        _preferences.update { it.copy(countryIso = iso) }
    }

    override suspend fun setTouchMode(touchMode: Boolean) {
        _preferences.update { it.copy(touchMode = touchMode) }
    }

    override suspend fun setRouterIp(ip: String) {
        _preferences.update { it.copy(routerIp = ip) }
    }

    override suspend fun setRefreshRateSeconds(seconds: Long) {
        _preferences.update { it.copy(refreshRateSeconds = seconds) }
    }

    override suspend fun resetToDefault() {
        _preferences.value = defaultPreferences
    }
}
