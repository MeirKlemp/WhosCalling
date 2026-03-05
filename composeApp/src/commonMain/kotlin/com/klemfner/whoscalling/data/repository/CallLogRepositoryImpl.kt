package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.util.currentTimeMillis
import com.klemfner.whoscalling.util.normalizePhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogRepositoryImpl(
    private val remoteDataSource: CallLogRemoteDataSource,
    private val localDataSource: CallLogLocalDataSource,
    private val scope: CoroutineScope,
    private val currentTimeMillis: () -> Long = ::currentTimeMillis,
    private val normalizePhone: (String) -> String = ::normalizePhoneNumber,
    private val refreshIntervalMs: Long = REFRESH_INTERVAL_MS,
) : CallLogRepository {

    private var autoRefreshJob: Job? = null

    override val callLogs: Flow<List<CallLog>> = localDataSource.callLogs

    init {
        startAutoRefresh()
    }

    override val incomingCallLog: Flow<CallLog?> = localDataSource.callLogs.map { logs ->
        val oneMinuteAgo = currentTimeMillis() - 60_000L
        logs.filter { it.type == CallType.INCOMING && it.timestamp >= oneMinuteAgo }
            .minByOrNull { it.timestamp }
    }

    override suspend fun refreshCallLogs() {
        localDataSource.replaceAllCallLogs(fetchAndNormalize())
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = scope.launch {
            while (true) {
                delay(refreshIntervalMs)
                localDataSource.replaceAllCallLogs(fetchAndNormalize())
            }
        }
    }

    private suspend fun fetchAndNormalize(): List<CallLog> {
        return remoteDataSource.getCallLogs().map { log ->
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
    }

    companion object {
        const val REFRESH_INTERVAL_MS = 5_000L
    }
}
