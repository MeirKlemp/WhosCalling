package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    val callLogs: Flow<List<CallLog>>
    val incomingCallLog: Flow<CallLog?>
    val autoRefreshCallLogs: Flow<Unit>
    suspend fun refreshCallLogs()
}
