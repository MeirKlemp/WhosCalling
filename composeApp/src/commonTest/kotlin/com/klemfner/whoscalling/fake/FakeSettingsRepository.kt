package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    initialIso: String = "US",
    val defaultIso: String = initialIso,
) : SettingsRepository {

    private val _countryIso = MutableStateFlow(initialIso)
    override val countryIso: StateFlow<String> = _countryIso.asStateFlow()

    override val currentCountryIso: String
        get() = _countryIso.value

    override suspend fun setCountryIso(iso: String) {
        _countryIso.value = iso
    }

    override suspend fun resetToDefault() {
        _countryIso.value = defaultIso
    }
}
