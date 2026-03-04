package com.klemfner.whoscalling.ui.common.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }
}

fun formatTimestamp(timestampMillis: Long): String {
    val tz = TimeZone.currentSystemDefault()
    val dateTime = Instant.fromEpochMilliseconds(timestampMillis).toLocalDateTime(tz)
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun formatShortDate(timestampMillis: Long): String {
    val tz = TimeZone.currentSystemDefault()
    val dateTime = Instant.fromEpochMilliseconds(timestampMillis).toLocalDateTime(tz)
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val year = (dateTime.year % 100).toString().padStart(2, '0')
    return "$day/$month/$year"
}
