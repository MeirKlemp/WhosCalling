package com.klemfner.whoscalling.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SettingsViewModel(
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "Settings"
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val _importResult = MutableStateFlow<ImportResult?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        contactRepository.contacts.map { it.size },
        settingsRepository.countryIso,
        _importResult,
    ) { count, iso, importResult ->
        SettingsUiState(
            contactCount = count,
            countryIso = iso,
            importResult = importResult,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState(countryIso = settingsRepository.currentCountryIso),
    )

    suspend fun exportContacts(): ExportData {
        val contacts = contactRepository.contacts.first()
        return ExportData(
            json = json.encodeToString<List<Contact>>(contacts),
            count = contacts.size,
        )
    }

    fun importContacts(jsonString: String) {
        viewModelScope.launch {
            try {
                val contacts = json.decodeFromString<List<Contact>>(jsonString)
                val imported = contactRepository.addContacts(contacts)
                _importResult.value = ImportResult.Success(imported)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to import contacts", e)
                _importResult.value = ImportResult.Error
            }
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun setCountryIso(iso: String) {
        viewModelScope.launch {
            settingsRepository.setCountryIso(iso)
        }
    }

    fun resetCountryIsoToDefault() {
        viewModelScope.launch {
            settingsRepository.resetToDefault()
        }
    }
}

data class ExportData(val json: String, val count: Int)

sealed interface ImportResult {
    data class Success(val count: Int) : ImportResult
    data object Error : ImportResult
}
