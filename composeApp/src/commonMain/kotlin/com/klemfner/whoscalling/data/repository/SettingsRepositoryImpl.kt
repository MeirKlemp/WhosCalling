package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.SettingsLocalDataSource
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsRepositoryImpl(
    private val localDataSource: SettingsLocalDataSource,
    private val defaultIso: String,
    private val scope: CoroutineScope,
) : SettingsRepository {

    override val countryIso: StateFlow<String> = localDataSource.countryIso
        .stateIn(scope, SharingStarted.Eagerly, defaultIso)

    override val currentCountryIso: String
        get() = countryIso.value

    override suspend fun setCountryIso(iso: String) {
        localDataSource.setCountryIso(iso)
    }

    override suspend fun resetToDefault() {
        localDataSource.setCountryIso(defaultIso)
    }
}
