package com.klemfner.whoscalling.ui.common.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatUtilsTest {

    @Test
    fun formatDurationSecondsOnly() {
        assertEquals("5s", formatDuration(5))
    }

    @Test
    fun formatDurationMinutesAndSeconds() {
        assertEquals("2m 30s", formatDuration(150))
    }

    @Test
    fun formatDurationHoursMinutesSeconds() {
        assertEquals("1h 5m 30s", formatDuration(3930))
    }

    @Test
    fun formatDurationZero() {
        assertEquals("0s", formatDuration(0))
    }

    @Test
    fun formatDurationExactMinute() {
        assertEquals("1m 0s", formatDuration(60))
    }

    @Test
    fun formatDurationExactHour() {
        assertEquals("1h 0m 0s", formatDuration(3600))
    }

    @Test
    fun formatTimestampFormatsCorrectly() {
        val result = formatTimestamp(1705328400000L)
        assertEquals(5, result.length)
        assertEquals(':', result[2])
    }

    @Test
    fun formatShortDateFormatsCorrectly() {
        val result = formatShortDate(1705328400000L)
        assertEquals(8, result.length)
        assertEquals('/', result[2])
        assertEquals('/', result[5])
    }

    @Test
    fun formatShortDatePartsArePadded() {
        val result = formatShortDate(1705328400000L)
        // Each part (dd, mm, yy) should be exactly 2 characters
        val parts = result.split('/')
        assertEquals(3, parts.size)
        parts.forEach { assertEquals(2, it.length) }
    }

    @Test
    fun formatShortDateYearIsTwoDigits() {
        val result = formatShortDate(1705328400000L)
        val year = result.substring(6, 8)
        assertEquals(2, year.length)
        assertEquals("24", year)
    }
}
