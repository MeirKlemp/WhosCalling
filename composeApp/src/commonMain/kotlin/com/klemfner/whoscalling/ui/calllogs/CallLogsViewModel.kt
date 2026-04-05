package com.klemfner.whoscalling.ui.calllogs

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

class CallLogsViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val spamRepository: SpamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallLogsUiState())
    val uiState: StateFlow<CallLogsUiState> = _uiState.asStateFlow()

    private val selectedNumber = MutableStateFlow<String?>(null)

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
            combine(
                sortedCallLogs,
                contactMap,
                selectedNumber,
                spamMap,
            ) { sorted, contacts, phone, spams ->
                val filtered = if (phone != null) {
                    sorted.filter { it.phoneNumber == phone }
                } else {
                    emptyList()
                }
                CallLogsData(
                    sorted = sorted,
                    contacts = contacts,
                    filtered = filtered,
                    spams = spams,
                )
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        callLogs = data.sorted,
                        contacts = data.contacts,
                        selectedNumberCallLogs = data.filtered,
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

    fun selectCallLog(callLog: CallLog) {
        selectedNumber.value = callLog.phoneNumber
        _uiState.update {
            it.copy(selectedCallLog = callLog, currentPane = CallLogsPane.DETAILS)
        }
    }

    fun selectCallLogById(callLogId: String) {
        val callLog = _uiState.value.callLogs.find { it.id == callLogId } ?: return
        selectCallLog(callLog)
    }

    fun goBack() {
        when (_uiState.value.currentPane) {
            CallLogsPane.DETAILS -> {
                selectedNumber.value = null
                _uiState.update {
                    it.copy(currentPane = CallLogsPane.LIST, selectedCallLog = null)
                }
            }
            CallLogsPane.LIST -> { /* nothing */ }
        }
    }

    fun requestReportSpam() {
        val phoneNumber = _uiState.value.selectedCallLog?.phoneNumber ?: return
        _uiState.update {
            it.copy(
                showReportSpamDialog = true,
                reportDialogPhoneNumber = phoneNumber,
            )
        }
    }

    fun requestReportSafe() {
        val phoneNumber = _uiState.value.selectedCallLog?.phoneNumber ?: return
        _uiState.update {
            it.copy(
                showTrustNumberDialog = true,
                reportDialogPhoneNumber = phoneNumber,
            )
        }
    }

    fun confirmReportSpam() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch {
            spamRepository.reportAsSpam(phoneNumber)
        }
        _uiState.update {
            it.copy(showReportSpamDialog = false, reportDialogPhoneNumber = "")
        }
    }

    fun confirmReportSafe() {
        val phoneNumber = _uiState.value.reportDialogPhoneNumber
        viewModelScope.launch {
            spamRepository.reportAsSafe(phoneNumber)
        }
        _uiState.update {
            it.copy(showTrustNumberDialog = false, reportDialogPhoneNumber = "")
        }
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

    companion object {
        private const val TAG = "CallLogsViewModel"
    }
}

private data class CallLogsData(
    val sorted: List<CallLog>,
    val contacts: Map<String, Contact>,
    val filtered: List<CallLog>,
    val spams: Map<String, Spam>,
)
