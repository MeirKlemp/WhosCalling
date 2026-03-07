package com.klemfner.whoscalling.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.util.getCountryIsoFromPhoneNumber
import com.klemfner.whoscalling.util.normalizePhoneNumberWithRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val selectedContactPhone = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            contactRepository.contacts.collect { contacts ->
                _uiState.update {
                    it.copy(contacts = contacts.sortedBy { c -> c.name.lowercase() })
                }
            }
        }
        viewModelScope.launch {
            combine(
                callLogRepository.callLogs,
                selectedContactPhone,
            ) { allLogs, phone ->
                val counts = allLogs.groupBy { it.phoneNumber }
                    .mapValues { (_, logs) -> logs.size }
                val filtered = if (phone != null) {
                    allLogs.filter { it.phoneNumber == phone }
                        .sortedByDescending { it.timestamp }
                } else {
                    emptyList()
                }
                Pair(counts, filtered)
            }.collect { (counts, filtered) ->
                _uiState.update { it.copy(callCounts = counts, contactCallLogs = filtered) }
            }
        }
        viewModelScope.launch {
            settingsRepository.countryIso.collect { iso ->
                _uiState.update { it.copy(defaultCountryIso = iso) }
            }
        }
    }

    fun selectContact(contact: Contact) {
        selectedContactPhone.value = contact.phoneNumber
        _uiState.update {
            it.copy(selectedContact = contact, currentPane = ContactsPane.DETAILS)
        }
    }

    fun openAddContact(phoneNumber: String = "") {
        val defaultIso = _uiState.value.defaultCountryIso
        _uiState.update {
            it.copy(
                currentPane = ContactsPane.FORM,
                formState = ContactFormState(
                    isNew = true,
                    phoneNumber = phoneNumber,
                    selectedCountryIso = defaultIso,
                ),
            )
        }
    }

    fun openEditContact() {
        val contact = _uiState.value.selectedContact ?: return
        val iso = getCountryIsoFromPhoneNumber(contact.phoneNumber)
            ?: _uiState.value.defaultCountryIso
        _uiState.update {
            it.copy(
                currentPane = ContactsPane.FORM,
                formState = ContactFormState(
                    id = contact.id,
                    name = contact.name,
                    phoneNumber = contact.phoneNumber,
                    email = contact.email ?: "",
                    isNew = false,
                    selectedCountryIso = iso,
                ),
            )
        }
    }

    fun goBack() {
        when (_uiState.value.currentPane) {
            ContactsPane.FORM -> {
                if (_uiState.value.formState.isNew) {
                    if (_uiState.value.selectedContact != null) {
                        _uiState.update { it.copy(currentPane = ContactsPane.DETAILS) }
                    } else {
                        _uiState.update { it.copy(currentPane = ContactsPane.LIST) }
                    }
                } else {
                    _uiState.update { it.copy(currentPane = ContactsPane.DETAILS) }
                }
            }
            ContactsPane.DETAILS -> {
                selectedContactPhone.value = null
                _uiState.update {
                    it.copy(currentPane = ContactsPane.LIST, selectedContact = null)
                }
            }
            ContactsPane.LIST -> { /* nothing */ }
        }
    }

    fun updateFormName(name: String) {
        _uiState.update { it.copy(formState = it.formState.copy(name = name)) }
    }

    fun updateFormPhone(phone: String) {
        _uiState.update { it.copy(formState = it.formState.copy(phoneNumber = phone)) }
    }

    fun updateFormEmail(email: String) {
        _uiState.update { it.copy(formState = it.formState.copy(email = email)) }
    }

    fun updateFormCountryIso(iso: String) {
        _uiState.update { it.copy(formState = it.formState.copy(selectedCountryIso = iso)) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveContact() {
        val form = _uiState.value.formState
        viewModelScope.launch {
            val phoneNumber = try {
                normalizePhoneNumberWithRegion(
                    form.phoneNumber,
                    form.selectedCountryIso.ifEmpty { null },
                )
            } catch (_: Exception) {
                form.phoneNumber
            }
            val contact = Contact(
                id = form.id ?: "",
                name = form.name,
                phoneNumber = phoneNumber,
                email = form.email.ifBlank { null },
            )
            try {
                contactRepository.addContact(contact)
                if (form.isNew) {
                    _uiState.update { it.copy(currentPane = ContactsPane.LIST) }
                } else {
                    selectedContactPhone.value = contact.phoneNumber
                    _uiState.update {
                        it.copy(
                            currentPane = ContactsPane.DETAILS,
                            selectedContact = contact,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
