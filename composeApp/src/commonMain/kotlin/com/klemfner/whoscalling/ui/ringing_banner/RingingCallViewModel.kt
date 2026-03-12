package com.klemfner.whoscalling.ui.ringing_banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RingingCallViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RingingCallUiState(),
    )
    val uiState = _uiState.asStateFlow()

    init {
        observeDefaultCountryIso()
        observeRingingCall()
    }

    private fun observeDefaultCountryIso() {
        viewModelScope.launch {
            settingsRepository.preferences.collect { preferences ->
                _uiState.update { it.copy(defaultCountryIso = preferences.countryIso) }
            }
        }
    }

    private fun observeRingingCall() {
        viewModelScope.launch {
            combine(
                callLogRepository.ringingCall,
                contactRepository.contacts,
            ) { ringingCall, contacts ->
                ringingCall?.let {
                    val ringingContact = contacts.find { it.phoneNumber == ringingCall.phoneNumber }
                    Pair(ringingCall, ringingContact)
                }
            }.collect { ringingCallAndContact ->
                if (ringingCallAndContact != null) {
                    val (ringingCall, ringingContact) = ringingCallAndContact
                    _uiState.update { state ->
                        val isDismissed =
                            state.isDismissed && ringingCall.id == state.ringingCall?.id
                        state.copy(
                            ringingCall = ringingCall,
                            contact = ringingContact,
                            isDismissed = isDismissed,
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            ringingCall = null,
                            contact = null,
                            isDismissed = false,
                        )
                    }
                }
            }
        }
    }

    fun dismiss() {
        _uiState.update { it.copy(isDismissed = true) }
    }
}