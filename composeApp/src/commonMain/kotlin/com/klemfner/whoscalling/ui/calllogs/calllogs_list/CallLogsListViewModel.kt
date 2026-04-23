package com.klemfner.whoscalling.ui.calllogs.calllogs_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallLogsListViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val spamRepository: SpamRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "CallLogsListViewModel"
    }

    private val _uiState = MutableStateFlow(CallLogsListUiState())
    val uiState: StateFlow<CallLogsListUiState> = _uiState.asStateFlow()

    init {
        val sortedCallLogs = callLogRepository.callLogs.map { logs ->
            logs.sortedByDescending { it.timestamp }
        }
        val contactMap = contactRepository.contacts.map { contacts ->
            contacts.associateBy { it.phoneNumber }
        }
        val spamMap = spamRepository.spams.map { spams ->
            spams.associateBy { it.phoneNumber }
        }

        viewModelScope.launch {
            combine(sortedCallLogs, contactMap, spamMap) { sorted, contacts, spams ->
                ListData(sorted, contacts, spams)
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        callLogs = data.sorted,
                        contacts = data.contacts,
                        spams = data.spams,
                    )
                }
            }
        }

        viewModelScope.launch {
            authRepository.loggedInUser.collect { user ->
                _uiState.update { it.copy(isLoggedIn = user != null) }
            }
        }

        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(defaultCountryIso = prefs.countryIso) }
            }
        }

        viewModelScope.launch {
            callLogRepository.ringingCall.collect { ringingCall ->
                _uiState.update { it.copy(ringingCallId = ringingCall?.id) }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, refreshError = false) }
        viewModelScope.launch {
            try {
                callLogRepository.refreshCallLogs()
            } catch (e: Exception) {
                Logger.e(TAG, "Manual refresh failed", e)
                _uiState.update { it.copy(refreshError = true) }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearRefreshError() {
        _uiState.update { it.copy(refreshError = false) }
    }
}

private data class ListData(
    val sorted: List<CallLog>,
    val contacts: Map<String, Contact>,
    val spams: Map<String, Spam>,
)
