package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog

class FakeCallLogRemoteDataSource : CallLogRemoteDataSource {
    private var callLogs: List<CallLog> = emptyList()

    fun emit(logs: List<CallLog>) {
        callLogs = logs
    }

    override suspend fun getCallLogs(token: String?): List<CallLog> = callLogs
}
