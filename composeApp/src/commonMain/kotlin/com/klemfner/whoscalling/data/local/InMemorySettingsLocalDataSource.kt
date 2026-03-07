package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.util.defaultCountryIso
import com.klemfner.whoscalling.util.defaultRouterIp
import com.klemfner.whoscalling.util.defaultTouchMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsLocalDataSource : SettingsLocalDataSource {
    private val _preferences = MutableStateFlow(
        UserPreferences(
            countryIso = defaultCountryIso(),
            touchMode = defaultTouchMode(),
            routerIp = defaultRouterIp(),
        ),
    )
    override val preferences: Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        _preferences.update(update)
    }
}
