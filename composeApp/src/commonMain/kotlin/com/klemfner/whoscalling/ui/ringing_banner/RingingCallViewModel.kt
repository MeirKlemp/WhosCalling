package com.klemfner.whoscalling.ui.ringing_banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RingingCallViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val spamRepository: SpamRepository,
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
                spamRepository.spams.map { spams ->
                    spams.filter { it.isSpam }.associateBy { it.phoneNumber }
                },
            ) { ringingCall, contacts, spamMap ->
                ringingCall?.let {
                    val ringingContact = contacts.find { it.phoneNumber == ringingCall.phoneNumber }
                    val isSpam = spamMap.containsKey(ringingCall.phoneNumber)
                    Triple(ringingCall, ringingContact, isSpam)
                }
            }.collect { ringingCallAndContact ->
                if (ringingCallAndContact != null) {
                    val (ringingCall, ringingContact, isSpam) = ringingCallAndContact
                    _uiState.update { state ->
                        val isDismissed =
                            state.isDismissed && ringingCall.id == state.ringingCall?.id
                        state.copy(
                            ringingCall = ringingCall,
                            contact = ringingContact,
                            isDismissed = isDismissed,
                            isSpam = isSpam,
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            ringingCall = null,
                            contact = null,
                            isDismissed = false,
                            isSpam = false,
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