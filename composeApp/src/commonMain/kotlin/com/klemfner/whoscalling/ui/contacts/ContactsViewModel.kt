package com.klemfner.whoscalling.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.InvalidPhoneNumberException
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import com.klemfner.whoscalling.util.getCountryIsoFromPhoneNumber
import com.klemfner.whoscalling.util.maskPhoneNumber
import com.klemfner.whoscalling.util.normalizePhoneNumber as normalizePhoneNumberImpl
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

class ContactsViewModel(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
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
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(defaultCountryIso = prefs.countryIso) }
            }
        }
        viewModelScope.launch {
            spamRepository.spams.map { spams ->
                spams.filter { it.isSpam }.associateBy { it.phoneNumber }
            }.collect { spamMap ->
                _uiState.update { it.copy(spamNumbers = spamMap) }
            }
        }
        viewModelScope.launch {
            combine(
                spamRepository.spams,
                selectedContactPhone,
            ) { allSpams, phone ->
                if (phone != null) {
                    allSpams.find { it.phoneNumber == phone }
                } else {
                    null
                }
            }.collect { spam ->
                _uiState.update { it.copy(selectedSpam = spam) }
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
        val extractedIso = if (phoneNumber.isNotEmpty()) getCountryIsoFromPhoneNumber(phoneNumber) else null
        val iso = extractedIso ?: defaultIso
        val nationalNumber = if (phoneNumber.isNotEmpty() && extractedIso != null) {
            formatPhoneForDisplay(phoneNumber, iso).nationalNumber
        } else {
            phoneNumber
        }
        _uiState.update {
            it.copy(
                currentPane = ContactsPane.FORM,
                formState = ContactFormState(
                    isNew = true,
                    phoneNumber = nationalNumber,
                    selectedCountryIso = iso,
                ),
            )
        }
    }

    fun openEditContact() {
        val contact = _uiState.value.selectedContact ?: return
        val extractedIso = getCountryIsoFromPhoneNumber(contact.phoneNumber)
        val iso = extractedIso ?: _uiState.value.defaultCountryIso
        val nationalNumber = if (extractedIso != null) {
            formatPhoneForDisplay(contact.phoneNumber, iso).nationalNumber
        } else {
            contact.phoneNumber
        }
        _uiState.update {
            it.copy(
                currentPane = ContactsPane.FORM,
                formState = ContactFormState(
                    id = contact.id,
                    name = contact.name,
                    phoneNumber = nationalNumber,
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
                    it.copy(currentPane = ContactsPane.LIST, selectedContact = null, selectedSpam = null)
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
        _uiState.update { it.copy(error = null) }
    }

    fun enterDeleteMode() {
        _uiState.update {
            it.copy(isDeleteMode = true, selectedForDeletion = emptySet())
        }
    }

    fun exitDeleteMode() {
        _uiState.update {
            it.copy(isDeleteMode = false, selectedForDeletion = emptySet())
        }
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
        _uiState.update {
            it.copy(selectedForDeletion = it.contacts.map { c -> c.id }.toSet())
        }
    }

    fun unselectAllContacts() {
        _uiState.update { it.copy(selectedForDeletion = emptySet()) }
    }

    fun requestDeleteSelectedContacts() {
        val state = _uiState.value
        val selectedIds = state.selectedForDeletion
        if (selectedIds.isEmpty()) return
        val contactName = if (selectedIds.size == 1) {
            state.contacts.find { it.id == selectedIds.first() }?.name
        } else {
            null
        }
        _uiState.update {
            it.copy(showDeleteDialog = true, deleteDialogContactName = contactName)
        }
    }

    fun requestDeleteContact(contact: Contact) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                deleteDialogContactName = contact.name,
                selectedForDeletion = setOf(contact.id),
            )
        }
    }

    fun confirmDelete() {
        val state = _uiState.value
        val idsToDelete = state.selectedForDeletion
        viewModelScope.launch {
            try {
                for (id in idsToDelete) {
                    contactRepository.deleteContact(id)
                }
                val deletedSelectedContact = state.selectedContact?.id in idsToDelete
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        deleteDialogContactName = null,
                        isDeleteMode = false,
                        selectedForDeletion = emptySet(),
                        selectedContact = if (deletedSelectedContact) null else it.selectedContact,
                        currentPane = if (deletedSelectedContact && it.currentPane == ContactsPane.DETAILS) {
                            ContactsPane.LIST
                        } else {
                            it.currentPane
                        },
                    )
                }
                if (deletedSelectedContact) {
                    selectedContactPhone.value = null
                }
            } catch (e: Exception) {
                Logger.e(TAG, "confirmDelete failed", e)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        deleteDialogContactName = null,
                        error = ContactsError.GenericDeleteError,
                    )
                }
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(showDeleteDialog = false, deleteDialogContactName = null)
        }
    }

    fun saveContact() {
        val form = _uiState.value.formState
        viewModelScope.launch {
            try {
                val phoneNumber = normalizePhoneNumber(
                    form.phoneNumber,
                    form.selectedCountryIso.ifEmpty { null },
                )
                val contact = Contact(
                    id = form.id ?: "",
                    name = form.name,
                    phoneNumber = phoneNumber,
                    email = form.email.ifBlank { null },
                )
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
                Logger.e(TAG, "saveContact failed: name=${form.name}, phone=${maskPhoneNumber(form.phoneNumber)}, iso=${form.selectedCountryIso}", e)
                val error =
                    if (e is InvalidPhoneNumberException) ContactsError.InvalidPhoneNumber
                    else ContactsError.GenericFormError
                _uiState.update { it.copy(error = error) }
            }
        }
    }

    fun requestReportSpam() {
        val state = _uiState.value
        val contact = state.selectedContact ?: return
        val displayName = "${contact.name} (${contact.phoneNumber})"
        _uiState.update {
            it.copy(
                showReportSpamDialog = true,
                reportDialogPhoneNumber = contact.phoneNumber,
                reportDialogDisplayName = displayName,
            )
        }
    }

    fun requestReportSafe() {
        val state = _uiState.value
        val contact = state.selectedContact ?: return
        val displayName = "${contact.name} (${contact.phoneNumber})"
        _uiState.update {
            it.copy(
                showTrustNumberDialog = true,
                reportDialogPhoneNumber = contact.phoneNumber,
                reportDialogDisplayName = displayName,
            )
        }
    }

    fun confirmReportSpam() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch {
            spamRepository.reportAsSpam(phoneNumber)
        }
        _uiState.update {
            it.copy(showReportSpamDialog = false, reportDialogPhoneNumber = "", reportDialogDisplayName = "")
        }
    }

    fun confirmReportSafe() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch {
            spamRepository.reportAsSafe(phoneNumber)
        }
        _uiState.update {
            it.copy(showTrustNumberDialog = false, reportDialogPhoneNumber = "", reportDialogDisplayName = "")
        }
    }

    fun dismissReportDialog() {
        _uiState.update {
            it.copy(
                showReportSpamDialog = false,
                showTrustNumberDialog = false,
                reportDialogPhoneNumber = "",
                reportDialogDisplayName = "",
            )
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String, defaultRegion: String? = null): String {
        return try {
            normalizePhoneNumberImpl(phoneNumber, defaultRegion)
        } catch (e: Exception) {
            throw InvalidPhoneNumberException(phoneNumber, cause = e)
        }
    }
}
