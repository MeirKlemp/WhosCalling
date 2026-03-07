package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.util.Logger
import com.klemfner.whoscalling.util.defaultCountryIso
import com.klemfner.whoscalling.util.defaultRouterIp
import com.klemfner.whoscalling.util.defaultTouchMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File

class FileSettingsLocalDataSource(
    private val settingsFile: File,
) : SettingsLocalDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    private val _preferences = MutableStateFlow(loadFromFile() ?: defaultPreferences())
    override val preferences: Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        _preferences.update { current ->
            val updated = update(current)
            saveToFile(updated)
            updated
        }
    }

    private fun defaultPreferences() = UserPreferences(
        countryIso = defaultCountryIso(),
        touchMode = defaultTouchMode(),
        routerIp = defaultRouterIp(),
    )

    private fun loadFromFile(): UserPreferences? {
        if (!settingsFile.exists()) return null
        return try {
            json.decodeFromString<UserPreferences>(settingsFile.readText())
        } catch (_: Exception) {
            null
        }
    }

    private fun saveToFile(preferences: UserPreferences) {
        try {
            settingsFile.parentFile?.mkdirs()
            settingsFile.writeText(json.encodeToString(UserPreferences.serializer(), preferences))
        } catch (e: Exception) {
            Logger.w("FileSettingsLocalDataSource", "Failed to save settings to file", e)
        }
    }
}
