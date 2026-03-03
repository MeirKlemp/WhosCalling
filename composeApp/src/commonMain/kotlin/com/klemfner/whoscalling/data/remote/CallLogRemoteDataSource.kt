package com.klemfner.whoscalling.data.remote

import com.klemfner.whoscalling.domain.model.CallLog
import kotlinx.coroutines.flow.Flow

interface CallLogRemoteDataSource {
    val callLogs: Flow<List<CallLog>>
    suspend fun refresh()
}
