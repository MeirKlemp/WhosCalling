package com.klemfner.whoscalling.ui.contacts.contacts_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsListViewModel(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val settingsRepository: SettingsRepository,
    private val spamRepository: SpamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ContactsListUiState(defaultCountryIso = settingsRepository.currentCountryIso),
    )
    val uiState: StateFlow<ContactsListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.contacts.collect { contacts ->
                _uiState.update {
                    it.copy(contacts = contacts.sortedBy { c -> c.name.lowercase() })
                }
            }
        }
        viewModelScope.launch {
            callLogRepository.callLogs.collect { logs ->
                val counts = logs.groupBy { it.phoneNumber }.mapValues { (_, l) -> l.size }
                _uiState.update { it.copy(callCounts = counts) }
            }
        }
        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(defaultCountryIso = prefs.countryIso) }
            }
        }
        viewModelScope.launch {
            spamRepository.spams.collect { spams ->
                _uiState.update { it.copy(spams = spams.associateBy { it.phoneNumber }) }
            }
        }
    }

    fun enterDeleteMode() {
        _uiState.update { it.copy(isDeleteMode = true, selectedForDeletion = emptySet()) }
    }

    fun exitDeleteMode() {
        _uiState.update { it.copy(isDeleteMode = false, selectedForDeletion = emptySet()) }
    }

    fun toggleContactSelection(contactId: String) {
        _uiState.update {
            val updated = if (contactId in it.selectedForDeletion) {
                it.selectedForDeletion - contactId
            } else {
                it.selectedForDeletion + contactId
            }
            it.copy(selectedForDeletion = updated)
        }
    }

    fun selectAllContacts() {
        _uiState.update { it.copy(selectedForDeletion = it.contacts.map { c -> c.id }.toSet()) }
    }

    fun unselectAllContacts() {
        _uiState.update { it.copy(selectedForDeletion = emptySet()) }
    }
}
