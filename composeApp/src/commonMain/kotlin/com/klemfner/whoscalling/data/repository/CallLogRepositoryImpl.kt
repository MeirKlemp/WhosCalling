package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.UnauthorizedException
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.util.currentTimeMillis
import com.klemfner.whoscalling.util.normalizePhoneNumber
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogRepositoryImpl(
    private val remoteDataSource: CallLogRemoteDataSource,
    private val localDataSource: CallLogLocalDataSource,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
    private val currentTimeMillis: () -> Long = ::currentTimeMillis,
    private val normalizePhone: (String) -> String = { normalizePhoneNumber(it) },
    private val refreshIntervalMs: Long = REFRESH_INTERVAL_MS,
) : CallLogRepository {

    private var autoRefreshJob: Job? = null

    override val callLogs: Flow<List<CallLog>> = localDataSource.callLogs

    init {
        scope.launch {
            authRepository.loggedInUser.collect { user ->
                if (user != null) {
                    startAutoRefresh()
                } else {
                    autoRefreshJob?.cancel()
                    autoRefreshJob = null
                }
            }
        }
    }

    override val incomingCallLog: Flow<CallLog?> = localDataSource.callLogs.map { logs ->
        val oneMinuteAgo = currentTimeMillis() - 60_000L
        logs.filter { it.type == CallType.INCOMING && it.timestamp >= oneMinuteAgo }
            .minByOrNull { it.timestamp }
    }

    override suspend fun refreshCallLogs() {
        localDataSource.replaceAllCallLogs(fetchWithAuth())
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = scope.launch {
            while (true) {
                delay(refreshIntervalMs)
                try {
                    localDataSource.replaceAllCallLogs(fetchWithAuth())
                } catch (e: Exception) {
                    Logger.e(TAG, "Auto-refresh failed", e)
                }
            }
        }
    }

    private suspend fun fetchWithAuth(): List<CallLog> {
        val token = authRepository.getToken()
        return try {
            fetchAndNormalize(token)
        } catch (e: UnauthorizedException) {
            authRepository.retryLogin()
            fetchAndNormalize(authRepository.getToken())
        }
    }

    private suspend fun fetchAndNormalize(token: String?): List<CallLog> {
        return remoteDataSource.getCallLogs(token).map { log ->
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
        private const val TAG = "CallLogRepository"
        const val REFRESH_INTERVAL_MS = 5_000L
    }
}
