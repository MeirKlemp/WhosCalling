package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeCallLogLocalDataSource : CallLogLocalDataSource {
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    override val callLogs: Flow<List<CallLog>> = _callLogs

    override suspend fun saveCallLogs(callLogs: List<CallLog>) {
        _callLogs.update { current -> current + callLogs }
    }

    override suspend fun replaceAllCallLogs(callLogs: List<CallLog>) {
        _callLogs.value = callLogs
    }

    override suspend fun deleteAllCallLogs() {
        _callLogs.value = emptyList()
    }
}
