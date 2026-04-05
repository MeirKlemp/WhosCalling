package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCallLogRepository(
    initialCallLogs: List<CallLog> = emptyList(),
    initialRingingCall: CallLog? = null
) : CallLogRepository {

    private val _callLogs = MutableStateFlow(initialCallLogs)
    override val callLogs: Flow<List<CallLog>> = _callLogs.asStateFlow()

    private val _ringingCall = MutableStateFlow(initialRingingCall)
    override val ringingCall: Flow<CallLog?> = _ringingCall.asStateFlow()

    var refreshException: Exception? = null
    private var pendingRefreshCallLogs: List<CallLog>? = null

    override suspend fun refreshCallLogs() {
        refreshException?.let { throw it }
        pendingRefreshCallLogs?.let { _callLogs.value = it }
    }

    // For testing: Mutators
    fun addCallLog(callLog: CallLog) {
        _callLogs.value += callLog
    }

    fun setCallLogs(callLogs: List<CallLog>) {
        _callLogs.value = callLogs
    }

    fun setRefreshCallLogs(callLogs: List<CallLog>) {
        pendingRefreshCallLogs = callLogs
    }

    fun setRingingCall(callLog: CallLog?) {
        _ringingCall.value = callLog
    }

    fun clear() {
        _callLogs.value = emptyList()
        _ringingCall.value = null
    }
}
