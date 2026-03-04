package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CallLogLocalDataSourceImpl(
    private val database: WhosCallingDatabase
) : CallLogLocalDataSource {

    override val callLogs: Flow<List<CallLog>> =
        database.callLogEntityQueries
            .getAllCallLogs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    CallLog(
                        id = entity.id,
                        phoneNumber = entity.phoneNumber,
                        type = CallType.valueOf(entity.type),
                        missed = entity.missed != 0L,
                        timestamp = entity.timestamp,
                        duration = entity.duration
                    )
                }
            }

    override suspend fun saveCallLogs(callLogs: List<CallLog>) {
        withContext(Dispatchers.Default) {
            database.callLogEntityQueries.transaction {
                callLogs.forEach { callLog ->
                    database.callLogEntityQueries.insertCallLog(
                        id = callLog.id,
                        phoneNumber = callLog.phoneNumber,
                        type = callLog.type.name,
                        missed = if (callLog.missed) 1L else 0L,
                        timestamp = callLog.timestamp,
                        duration = callLog.duration
                    )
                }
            }
        }
    }

    override suspend fun replaceAllCallLogs(callLogs: List<CallLog>) {
        withContext(Dispatchers.Default) {
            database.callLogEntityQueries.transaction {
                database.callLogEntityQueries.deleteAllCallLogs()
                callLogs.forEach { callLog ->
                    database.callLogEntityQueries.insertCallLog(
                        id = callLog.id,
                        phoneNumber = callLog.phoneNumber,
                        type = callLog.type.name,
                        missed = if (callLog.missed) 1L else 0L,
                        timestamp = callLog.timestamp,
                        duration = callLog.duration
                    )
                }
            }
        }
    }

    override suspend fun deleteAllCallLogs() {
        withContext(Dispatchers.Default) {
            database.callLogEntityQueries.deleteAllCallLogs()
        }
    }
}
