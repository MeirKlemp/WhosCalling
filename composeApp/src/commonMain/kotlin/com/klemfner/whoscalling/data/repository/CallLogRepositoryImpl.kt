package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.util.currentTimeMillis
import com.klemfner.whoscalling.util.normalizePhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallLogRepositoryImpl(
    private val remoteDataSource: CallLogRemoteDataSource,
    private val localDataSource: CallLogLocalDataSource,
    private val currentTimeMillis: () -> Long = ::currentTimeMillis,
    private val normalizePhone: (String) -> String = ::normalizePhoneNumber
) : CallLogRepository {

    override val callLogs: Flow<List<CallLog>> = localDataSource.callLogs

    override val incomingCallLog: Flow<CallLog?> = localDataSource.callLogs.map { logs ->
        val oneMinuteAgo = currentTimeMillis() - 60_000L
        logs.filter { it.type == CallType.INCOMING && it.timestamp >= oneMinuteAgo }
            .minByOrNull { it.timestamp }
    }

    override suspend fun refreshCallLogs() {
        val remoteLogs = remoteDataSource.getCallLogs().map { log ->
            val normalized = try {
                normalizePhone(log.phoneNumber)
            } catch (_: Exception) {
                log.phoneNumber
            }
            log.copy(
                phoneNumber = normalized,
                id = "$normalized-${log.timestamp}"
            )
        }
        localDataSource.deleteAllCallLogs()
        localDataSource.saveCallLogs(remoteLogs)
    }
}
