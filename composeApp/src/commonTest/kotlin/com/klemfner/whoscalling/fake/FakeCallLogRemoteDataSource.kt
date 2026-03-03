package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCallLogRemoteDataSource : CallLogRemoteDataSource {
    private val callLogs = MutableStateFlow<List<CallLog>>(emptyList())

    fun emit(logs: List<CallLog>) {
        callLogs.value = logs
    }

    override fun getCallLogs(): Flow<List<CallLog>> = callLogs
}
