package com.klemfner.whoscalling.data.remote

import com.klemfner.whoscalling.domain.model.CallLog

interface CallLogRemoteDataSource {
    suspend fun getCallLogs(token: String?): List<CallLog>
}
