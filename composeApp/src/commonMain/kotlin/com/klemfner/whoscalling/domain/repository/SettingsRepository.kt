package com.klemfner.whoscalling.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val countryIso: StateFlow<String>
    val currentCountryIso: String
    suspend fun setCountryIso(iso: String)
    suspend fun resetToDefault()
}
