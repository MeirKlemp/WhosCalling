package com.klemfner.whoscalling.ui.calllogs.calllog_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallLogDetailsViewModel(
    private val callLogRepository: CallLogRepository,
    private val spamRepository: SpamRepository,
) : ViewModel() {

    private val _selectedPhone = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(CallLogDetailsUiState())
    val uiState: StateFlow<CallLogDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _selectedPhone,
                callLogRepository.callLogs,
                spamRepository.spams,
            ) { phone, logs, spams ->
                val filtered = if (phone != null) {
                    logs.filter { it.phoneNumber == phone }.sortedByDescending { it.timestamp }
                } else {
                    emptyList()
                }
                CallLogDetailsUiState(
                    selectedNumberCallLogs = filtered,
                    spams = spams.associateBy { it.phoneNumber },
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun setSelectedPhone(phone: String?) {
        _selectedPhone.value = phone
    }
}
