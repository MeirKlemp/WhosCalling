package com.klemfner.whoscalling.data.remote

import com.fleeksoft.ksoup.Ksoup
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.UnauthorizedException
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

// TODO: Make router IP configurable - create a GitHub issue to track this.
//  Currently hardcoded to 192.168.60.1. Should be configurable via settings/preferences.
private const val ROUTER_BASE_URL = "http://192.168.60.1"

class PartnerCallLogsDataSource(
    private val httpClient: HttpClient,
) : CallLogRemoteDataSource {

    override suspend fun getCallLogs(token: String?): List<CallLog> = withContext(Dispatchers.Default) {
        val response = httpClient.get("$ROUTER_BASE_URL/modals/mmpbx-log-modal.lp") {
            token?.let { header("Cookie", "sessionID=$it") }
        }

        val html = response.bodyAsText()

        val document = Ksoup.parse(html)

        val table = document.selectFirst("#calllog")
            ?: throw UnauthorizedException()

        table.select("tbody tr")
            .filter { it.select("td").size >= 5 }
            .mapIndexed { index, row ->
            val cells = row.select("td")
            val time = cells[0].text()
            val callType = cells[1].text()
            val remoteNumber = cells[3].text()
            val duration = cells[4].text()

            val (type, missed) = parseCallType(callType)

            CallLog(
                id = index.toString(),
                phoneNumber = remoteNumber,
                type = type,
                missed = missed,
                timestamp = parseTimestamp(time),
                duration = parseDuration(duration),
            )
        }
    }

    private fun parseCallType(callType: String): Pair<CallType, Boolean> {
        return when (callType) {
            "Incoming Successful" -> CallType.INCOMING to false
            "Incoming Missed" -> CallType.INCOMING to true
            "Outgoing Successful" -> CallType.OUTGOING to false
            "Outgoing Missed" -> CallType.OUTGOING to true
            else -> throw IllegalArgumentException("Unknown call type: $callType")
        }
    }

    private fun parseTimestamp(time: String): Long {
        val dateTime = LocalDateTime.parse(time.replace(" ", "T"))
        return dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    private fun parseDuration(duration: String): Long {
        val cleaned = duration.trimEnd('s')
        val parts = cleaned.split(":")
        return parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()
    }
}
