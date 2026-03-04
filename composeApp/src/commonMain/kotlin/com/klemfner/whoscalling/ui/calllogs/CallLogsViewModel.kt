package com.klemfner.whoscalling.ui.calllogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallLogsViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallLogsUiState())
    val uiState: StateFlow<CallLogsUiState> = _uiState.asStateFlow()

    private val selectedNumber = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            combine(
                callLogRepository.callLogs,
                contactRepository.contacts,
                selectedNumber,
            ) { allLogs, contacts, phone ->
                val contactMap = contacts.associateBy { it.phoneNumber }
                val sorted = allLogs.sortedByDescending { it.timestamp }
                val filtered = if (phone != null) {
                    sorted.filter { it.phoneNumber == phone }
                } else {
                    emptyList()
                }
                Triple(sorted, contactMap, filtered)
            }.collect { (sorted, contactMap, filtered) ->
                _uiState.update {
                    it.copy(
                        callLogs = sorted,
                        contacts = contactMap,
                        selectedNumberCallLogs = filtered,
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            callLogRepository.refreshCallLogs()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectCallLog(callLog: CallLog) {
        selectedNumber.value = callLog.phoneNumber
        _uiState.update {
            it.copy(selectedCallLog = callLog, currentPane = CallLogsPane.DETAILS)
        }
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
}
