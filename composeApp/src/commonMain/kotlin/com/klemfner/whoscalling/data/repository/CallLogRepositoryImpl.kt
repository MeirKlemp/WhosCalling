package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.data.remote.IncomingCallDataSource
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.IncomingCall
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CallLogRepositoryImpl(
    private val remoteDataSource: CallLogRemoteDataSource,
    private val localDataSource: CallLogLocalDataSource,
    private val incomingCallDataSource: IncomingCallDataSource
) : CallLogRepository {

    override fun getCallLogs(): Flow<List<CallLog>> {
        return localDataSource.getCallLogs()
    }

    override suspend fun refreshCallLogs() {
        val remoteLogs = remoteDataSource.getCallLogs().first()
        localDataSource.deleteAllCallLogs()
        localDataSource.saveCallLogs(remoteLogs)
    }

    override fun observeIncomingCall(): Flow<IncomingCall?> {
        return incomingCallDataSource.observeIncomingCall()
    }
}
