package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.SettingsLocalDataSource
import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.util.defaultCountryIso
import com.klemfner.whoscalling.util.defaultTouchMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsRepositoryImpl(
    private val localDataSource: SettingsLocalDataSource,
    private val scope: CoroutineScope,
    private val defaultCountryIso: () -> String = ::defaultCountryIso,
    private val defaultTouchMode: () -> Boolean = ::defaultTouchMode,
) : SettingsRepository {

    override val preferences: StateFlow<UserPreferences> = localDataSource.preferences
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            UserPreferences(countryIso = defaultCountryIso(), touchMode = defaultTouchMode()),
        )

    override val currentCountryIso: String
        get() = preferences.value.countryIso

    override val currentTouchMode: Boolean
        get() = preferences.value.touchMode

    override suspend fun setCountryIso(iso: String) {
        localDataSource.updatePreferences { it.copy(countryIso = iso) }
    }

    override suspend fun setTouchMode(touchMode: Boolean) {
        localDataSource.updatePreferences { it.copy(touchMode = touchMode) }
    }

    override suspend fun resetToDefault() {
        localDataSource.updatePreferences { _ ->
            UserPreferences(countryIso = defaultCountryIso(), touchMode = defaultTouchMode())
        }
    }
}
