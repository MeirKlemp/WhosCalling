package com.klemfner.whoscalling.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val spamRepository: SpamRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "ContactsViewModel"
    }

    private val _uiState = MutableStateFlow(
        ContactsUiState(defaultCountryIso = settingsRepository.currentCountryIso),
    )
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(defaultCountryIso = prefs.countryIso) }
            }
        }
    }

    fun selectContact(contact: Contact) {
        _uiState.update {
            it.copy(selectedContact = contact, currentPane = ContactsPane.DETAILS)
        }
    }

    fun openAddContact(phoneNumber: String = "") {
        _uiState.update {
            it.copy(
                currentPane = ContactsPane.FORM,
                formMode = ContactFormMode.NEW,
                newContactPhone = phoneNumber,
            )
        }
    }

    fun openEditContact() {
        _uiState.update {
            it.copy(currentPane = ContactsPane.FORM, formMode = ContactFormMode.EDIT)
        }
    }

    fun goBack() {
        val state = _uiState.value
        when (state.currentPane) {
            ContactsPane.FORM -> {
                if (state.formMode == ContactFormMode.NEW) {
                    if (state.selectedContact != null) {
                        _uiState.update { it.copy(currentPane = ContactsPane.DETAILS) }
                    } else {
                        _uiState.update { it.copy(currentPane = ContactsPane.LIST) }
                    }
                } else {
                    _uiState.update { it.copy(currentPane = ContactsPane.DETAILS) }
                }
            }
            ContactsPane.DETAILS -> {
                _uiState.update { it.copy(currentPane = ContactsPane.LIST, selectedContact = null) }
            }
            ContactsPane.LIST -> { /* nothing */ }
        }
    }

    fun onFormSavedNew() {
        _uiState.update { it.copy(currentPane = ContactsPane.LIST) }
    }

    fun onFormSavedEdit(contact: Contact) {
        _uiState.update {
            it.copy(currentPane = ContactsPane.DETAILS, selectedContact = contact)
        }
    }

    fun requestDeleteContact(contact: Contact) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                deleteDialogContactName = contact.name,
                pendingDeleteIds = setOf(contact.id),
            )
        }
    }

    fun requestDeleteSelectedContacts(selectedIds: Set<String>, contacts: List<Contact>) {
        if (selectedIds.isEmpty()) return
        val contactName = if (selectedIds.size == 1) {
            contacts.find { it.id == selectedIds.first() }?.name
        } else {
            null
        }
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                deleteDialogContactName = contactName,
                pendingDeleteIds = selectedIds,
            )
        }
    }

    fun confirmDelete() {
        val state = _uiState.value
        val ids = state.pendingDeleteIds
        viewModelScope.launch {
            try {
                for (id in ids) {
                    contactRepository.deleteContact(id)
                }
                val deletedSelectedContact = state.selectedContact?.id in ids
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        deleteDialogContactName = null,
                        pendingDeleteIds = emptySet(),
                        selectedContact = if (deletedSelectedContact) null else it.selectedContact,
                        currentPane = if (deletedSelectedContact && it.currentPane == ContactsPane.DETAILS) {
                            ContactsPane.LIST
                        } else {
                            it.currentPane
                        },
                    )
                }
            } catch (e: Exception) {
                Logger.e(TAG, "confirmDelete failed", e)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        deleteDialogContactName = null,
                        pendingDeleteIds = emptySet(),
                    )
                }
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, deleteDialogContactName = null) }
    }

    fun requestReportSpam(phoneNumber: String) {
        _uiState.update { it.copy(showReportSpamDialog = true, reportDialogPhoneNumber = phoneNumber) }
    }

    fun requestReportSafe(phoneNumber: String) {
        _uiState.update { it.copy(showTrustNumberDialog = true, reportDialogPhoneNumber = phoneNumber) }
    }

    fun confirmReportSpam() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch { spamRepository.reportAsSpam(phoneNumber) }
        _uiState.update { it.copy(showReportSpamDialog = false, reportDialogPhoneNumber = "") }
    }

    fun confirmReportSafe() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch { spamRepository.reportAsSafe(phoneNumber) }
        _uiState.update { it.copy(showTrustNumberDialog = false, reportDialogPhoneNumber = "") }
    }

    fun dismissReportDialog() {
        _uiState.update {
            it.copy(
                showReportSpamDialog = false,
                showTrustNumberDialog = false,
                reportDialogPhoneNumber = "",
            )
        }
    }
}
