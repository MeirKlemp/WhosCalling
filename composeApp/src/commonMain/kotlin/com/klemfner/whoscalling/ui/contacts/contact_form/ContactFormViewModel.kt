package com.klemfner.whoscalling.ui.contacts.contact_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.InvalidPhoneNumberException
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.ui.contacts.ContactsError
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import com.klemfner.whoscalling.util.getCountryIsoFromPhoneNumber
import com.klemfner.whoscalling.util.maskPhoneNumber
import com.klemfner.whoscalling.util.Logger
import com.klemfner.whoscalling.util.normalizePhoneNumber as normalizePhoneNumberImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

class ContactFormViewModel(
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "ContactFormViewModel"
    }

    private val _uiState = MutableStateFlow(ContactFormUiState())
    val uiState: StateFlow<ContactFormUiState> = _uiState.asStateFlow()

    private val _saveEvent = MutableSharedFlow<ContactFormSaveEvent>()
    val saveEvent: SharedFlow<ContactFormSaveEvent> = _saveEvent.asSharedFlow()

    fun initForNew(phoneNumber: String = "", defaultCountryIso: String = "") {
        val extractedIso = if (phoneNumber.isNotEmpty()) getCountryIsoFromPhoneNumber(phoneNumber) else null
        val iso = extractedIso ?: defaultCountryIso
        val nationalNumber = if (phoneNumber.isNotEmpty() && extractedIso != null) {
            formatPhoneForDisplay(phoneNumber, iso).nationalNumber
        } else {
            phoneNumber
        }
        _uiState.update {
            it.copy(
                formState = ContactFormState(
                    isNew = true,
                    phoneNumber = nationalNumber,
                    selectedCountryIso = iso,
                ),
                error = null,
            )
        }
    }

    fun initForEdit(contact: Contact, defaultCountryIso: String = "") {
        val extractedIso = getCountryIsoFromPhoneNumber(contact.phoneNumber)
        val iso = extractedIso ?: defaultCountryIso
        val nationalNumber = if (extractedIso != null) {
            formatPhoneForDisplay(contact.phoneNumber, iso).nationalNumber
        } else {
            contact.phoneNumber
        }
        _uiState.update {
            it.copy(
                formState = ContactFormState(
                    id = contact.id,
                    name = contact.name,
                    phoneNumber = nationalNumber,
                    email = contact.email ?: "",
                    isNew = false,
                    selectedCountryIso = iso,
                ),
                error = null,
            )
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
                _saveEvent.emit(
                    if (form.isNew) ContactFormSaveEvent.SavedNew
                    else ContactFormSaveEvent.SavedEdit(contact),
                )
            } catch (e: Exception) {
                Logger.e(TAG, "saveContact failed: name=${form.name}, phone=${maskPhoneNumber(form.phoneNumber)}, iso=${form.selectedCountryIso}", e)
                val error = if (e is InvalidPhoneNumberException) {
                    ContactsError.InvalidPhoneNumber
                } else {
                    ContactsError.GenericFormError
                }
                _uiState.update { it.copy(error = error as ContactsError.FormError) }
            }
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
