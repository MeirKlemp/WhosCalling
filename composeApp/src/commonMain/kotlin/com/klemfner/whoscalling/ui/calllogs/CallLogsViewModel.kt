package com.klemfner.whoscalling.ui.calllogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.repository.SpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallLogsViewModel(
    private val spamRepository: SpamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallLogsUiState())
    val uiState: StateFlow<CallLogsUiState> = _uiState.asStateFlow()

    fun selectCallLog(callLog: CallLog) {
        _uiState.update { it.copy(selectedCallLog = callLog, currentPane = CallLogsPane.DETAILS) }
    }

    fun goBack() {
        when (_uiState.value.currentPane) {
            CallLogsPane.DETAILS -> {
                _uiState.update { it.copy(currentPane = CallLogsPane.LIST, selectedCallLog = null) }
            }
            CallLogsPane.LIST -> { /* nothing */ }
        }
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
