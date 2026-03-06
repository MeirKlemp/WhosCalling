package com.klemfner.whoscalling.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SettingsViewModel(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "Settings"
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

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
}

data class ExportData(val json: String, val count: Int)

sealed interface ImportResult {
    data class Success(val count: Int) : ImportResult
    data object Error : ImportResult
}
