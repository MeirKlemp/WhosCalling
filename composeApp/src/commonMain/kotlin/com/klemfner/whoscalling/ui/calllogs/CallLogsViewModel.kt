package com.klemfner.whoscalling.ui.calllogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
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

        viewModelScope.launch {
            combine(
                sortedCallLogs,
                contactMap,
                selectedNumber,
            ) { sorted, contacts, phone ->
                val filtered = if (phone != null) {
                    sorted.filter { it.phoneNumber == phone }
                } else {
                    emptyList()
                }
                Triple(sorted, contacts, filtered)
            }.collect { (sorted, contacts, filtered) ->
                _uiState.update {
                    it.copy(
                        callLogs = sorted,
                        contacts = contacts,
                        selectedNumberCallLogs = filtered,
                    )
                }
            }
        }

        viewModelScope.launch {
            authRepository.loggedInUser.collect { user ->
                _uiState.update { it.copy(isLoggedIn = user != null) }
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

    companion object {
        private const val TAG = "CallLogsViewModel"
    }
}
