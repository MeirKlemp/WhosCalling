package com.klemfner.whoscalling.ui.common.utils

import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TimePeriod {
    TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LONG_TIME_AGO
}

fun getTimePeriod(timestampMillis: Long): TimePeriod {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz).date
    val date = Instant.fromEpochMilliseconds(timestampMillis).toLocalDateTime(tz).date

    val daysDiff = (now.toEpochDays() - date.toEpochDays()).toInt()

    return when {
        daysDiff == 0 -> TimePeriod.TODAY
        daysDiff == 1 -> TimePeriod.YESTERDAY
        daysDiff in 2..6 -> TimePeriod.THIS_WEEK
        date.month == now.month && date.year == now.year -> TimePeriod.THIS_MONTH
        else -> TimePeriod.LONG_TIME_AGO
    }
}
