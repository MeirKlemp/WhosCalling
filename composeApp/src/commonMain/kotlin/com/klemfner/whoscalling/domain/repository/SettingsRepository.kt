package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.UserPreferences
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val preferences: StateFlow<UserPreferences>
    val currentCountryIso: String
    val currentTouchMode: Boolean
    suspend fun setCountryIso(iso: String)
    suspend fun setTouchMode(touchMode: Boolean)
    suspend fun resetToDefault()
}
