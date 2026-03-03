package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeCallLogLocalDataSource : CallLogLocalDataSource {
    private val callLogs = MutableStateFlow<List<CallLog>>(emptyList())

    override fun getCallLogs(): Flow<List<CallLog>> = callLogs

    override suspend fun saveCallLogs(callLogs: List<CallLog>) {
        this.callLogs.update { current -> current + callLogs }
    }

    override suspend fun deleteAllCallLogs() {
        callLogs.value = emptyList()
    }
}
