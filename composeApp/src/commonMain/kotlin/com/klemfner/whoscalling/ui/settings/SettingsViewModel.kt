package com.klemfner.whoscalling.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.ThemeMode
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
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
    private val spamRepository: SpamRepository,
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
        spamRepository.spams.map { it.size },
        settingsRepository.preferences,
        _importResult,
    ) { contactCount, spamCount, prefs, importResult ->
        SettingsUiState(
            contactCount = contactCount,
            spamCount = spamCount,
            countryIso = prefs.countryIso,
            touchMode = prefs.touchMode,
            routerIp = prefs.routerIp,
            refreshRateSeconds = prefs.refreshRateSeconds,
            themeMode = prefs.themeMode,
            importResult = importResult,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        with(settingsRepository) {
            SettingsUiState(
                countryIso = currentCountryIso,
                touchMode = currentTouchMode,
                routerIp = currentRouterIp,
                refreshRateSeconds = currentRefreshRateSeconds,
                themeMode = currentThemeMode,
            )
        },
    )

    suspend fun exportContacts(): ExportData {
        val contacts = contactRepository.contacts.first()
        val spams = spamRepository.spams.first()
        return ExportData(
            json = json.encodeToString<ExportPayload>(ExportPayload(contacts, spams)),
            contactCount = contacts.size,
            spamCount = spams.size,
        )
    }

    fun importContacts(jsonString: String) {
        viewModelScope.launch {
            try {
                // Try new format first
                val payload = try {
                    json.decodeFromString<ExportPayload>(jsonString)
                } catch (_: Exception) {
                    // Fall back to old format (list of contacts only)
                    val contacts = json.decodeFromString<List<Contact>>(jsonString)
                    ExportPayload(contacts, emptyList())
                }
                val importedContacts = contactRepository.addContacts(payload.contacts)
                val importedSpams = spamRepository.addSpams(payload.spams)
                _importResult.value = ImportResult.Success(importedContacts, importedSpams)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to import data", e)
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

    fun setTouchMode(touchMode: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTouchMode(touchMode)
        }
    }

    fun setRouterIp(ip: String) {
        viewModelScope.launch {
            settingsRepository.setRouterIp(ip)
        }
    }

    fun setRefreshRateSeconds(seconds: Long) {
        viewModelScope.launch {
            settingsRepository.setRefreshRateSeconds(seconds)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            settingsRepository.resetToDefault()
        }
    }
}

@kotlinx.serialization.Serializable
data class ExportPayload(
    val contacts: List<Contact>,
    val spams: List<Spam> = emptyList(),
)

data class ExportData(val json: String, val contactCount: Int, val spamCount: Int = 0)

sealed interface ImportResult {
    data class Success(val count: Int, val spamCount: Int = 0) : ImportResult
    data object Error : ImportResult
}

val ImportResult?.contactCount: Int
    get() = when (this) {
        is ImportResult.Success -> count
        else -> 0
    }
