package com.klemfner.whoscalling.data.remote

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType

class DummyCallLogRemoteDataSource : CallLogRemoteDataSource {
    override suspend fun getCallLogs(): List<CallLog> = dummyCallLogs
}

private val dummyCallLogs = listOf(
    // 5 logs with the same number
    CallLog("d1", "+1234567890", CallType.INCOMING, false, 1709300000000L, 120L),
    CallLog("d2", "+1234567890", CallType.OUTGOING, false, 1709290000000L, 300L),
    CallLog("d3", "+1234567890", CallType.INCOMING, true, 1709280000000L, 0L),
    CallLog("d4", "+1234567890", CallType.OUTGOING, false, 1709270000000L, 45L),
    CallLog("d5", "+1234567890", CallType.INCOMING, false, 1709260000000L, 200L),
    // 2 logs with the same number
    CallLog("d6", "+0987654321", CallType.OUTGOING, false, 1709250000000L, 60L),
    CallLog("d7", "+0987654321", CallType.INCOMING, true, 1709240000000L, 0L),
    // 3 logs with different numbers
    CallLog("d8", "+1112223333", CallType.INCOMING, false, 1709230000000L, 180L),
    CallLog("d9", "+4445556666", CallType.OUTGOING, false, 1709220000000L, 90L),
    CallLog("d10", "+7778889999", CallType.INCOMING, true, 1709210000000L, 0L),
)
