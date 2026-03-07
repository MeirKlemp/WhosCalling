package com.klemfner.whoscalling.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySettingsLocalDataSource(defaultIso: String) : SettingsLocalDataSource {
    private val _countryIso = MutableStateFlow(defaultIso)
    override val countryIso: Flow<String> = _countryIso.asStateFlow()

    override suspend fun setCountryIso(iso: String) {
        _countryIso.value = iso
    }
}
