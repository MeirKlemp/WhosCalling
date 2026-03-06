package com.klemfner.whoscalling.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SettingsViewModel(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val json = Json { prettyPrint = true }

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    suspend fun exportContacts(): String {
        val contacts = contactRepository.contacts.first()
        return json.encodeToString<List<Contact>>(contacts)
    }

    fun importContacts(jsonString: String) {
        viewModelScope.launch {
            try {
                val contacts = json.decodeFromString<List<Contact>>(jsonString)
                var imported = 0
                for (contact in contacts) {
                    try {
                        contactRepository.addContact(contact)
                        imported++
                    } catch (_: Exception) {
                        // Skip contacts that fail validation
                    }
                }
                _importResult.value = ImportResult.Success(imported)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error
            }
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}

sealed interface ImportResult {
    data class Success(val count: Int) : ImportResult
    data object Error : ImportResult
}
