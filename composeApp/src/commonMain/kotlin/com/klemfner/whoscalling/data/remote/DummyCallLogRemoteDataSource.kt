package com.klemfner.whoscalling.data.remote

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.util.currentTimeMillis

class DummyCallLogRemoteDataSource : CallLogRemoteDataSource {
    override suspend fun getCallLogs(token: String?): List<CallLog> =
        listOf(
            CallLog("ringing", "+97223456789", CallType.INCOMING, true, currentTimeMillis(), 0L),
        ) + dummyCallLogs
}

private val dummyCallLogs = listOf(
    // 5 logs with the same number
    CallLog("d1", "+97223456789", CallType.INCOMING, false, 1709300000000L, 120L),
    CallLog("d2", "+97223456789", CallType.OUTGOING, false, 1709290000000L, 300L),
    CallLog("d3", "+97223456789", CallType.INCOMING, true, 1709280000000L, 0L),
    CallLog("d4", "+97223456789", CallType.OUTGOING, false, 1709270000000L, 45L),
    CallLog("d5", "+97223456789", CallType.INCOMING, false, 1709260000000L, 200L),
    // 2 logs with the same number
    CallLog("d6", "+97234567890", CallType.OUTGOING, false, 1709250000000L, 60L),
    CallLog("d7", "+97234567890", CallType.INCOMING, true, 1709240000000L, 0L),
    // 3 logs with different numbers
    CallLog("d8", "+97226543210", CallType.INCOMING, false, 1709230000000L, 180L),
    CallLog("d9", "+97237654321", CallType.OUTGOING, false, 1709220000000L, 90L),
    CallLog("d10", "+97248765432", CallType.INCOMING, true, 1709210000000L, 0L),
)
