package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCallLogRemoteDataSource : CallLogRemoteDataSource {
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    override val callLogs: Flow<List<CallLog>> = _callLogs

    fun emit(logs: List<CallLog>) {
        _callLogs.value = logs
    }

    override suspend fun refresh() {
        // In the fake, data is pre-set via emit()
    }
}
