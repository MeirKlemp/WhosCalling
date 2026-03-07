package com.klemfner.whoscalling.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class FileSettingsLocalDataSource(
    private val settingsFile: File,
    private val defaultIso: String,
) : SettingsLocalDataSource {

    private val _countryIso = MutableStateFlow(loadFromFile() ?: defaultIso)
    override val countryIso: Flow<String> = _countryIso.asStateFlow()

    override suspend fun setCountryIso(iso: String) {
        settingsFile.parentFile?.mkdirs()
        settingsFile.writeText(iso)
        _countryIso.value = iso
    }

    private fun loadFromFile(): String? {
        if (!settingsFile.exists()) return null
        return try {
            settingsFile.readText().trim().ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }
}
