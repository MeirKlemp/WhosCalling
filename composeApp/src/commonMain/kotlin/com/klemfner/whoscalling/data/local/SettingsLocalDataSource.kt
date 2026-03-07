package com.klemfner.whoscalling.data.local

import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    val countryIso: Flow<String>
    suspend fun setCountryIso(iso: String)
}
