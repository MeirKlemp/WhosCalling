package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow

interface CallLogLocalDataSource {
    val callLogs: Flow<List<CallLog>>
    suspend fun saveCallLogs(callLogs: List<CallLog>)
    suspend fun replaceAllCallLogs(callLogs: List<CallLog>)
    suspend fun deleteAllCallLogs()
}
