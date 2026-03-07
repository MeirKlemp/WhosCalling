package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    val preferences: Flow<UserPreferences>
    suspend fun updatePreferences(update: (UserPreferences) -> UserPreferences)
}
