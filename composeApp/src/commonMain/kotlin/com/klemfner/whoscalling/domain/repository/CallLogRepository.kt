package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.IncomingCall
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    fun getCallLogs(): Flow<List<CallLog>>
    suspend fun refreshCallLogs()
    fun observeIncomingCall(): Flow<IncomingCall?>
}
