package com.klemfner.whoscalling.ui.settings

import com.klemfner.whoscalling.ui.settings.components.formatDuration
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatDurationTest {

    @Test
    fun formatDuration_zero() {
        assertEquals("0s", formatDuration(0))
    }

    @Test
    fun formatDuration_secondsOnly() {
        assertEquals("5s", formatDuration(5))
        assertEquals("30s", formatDuration(30))
        assertEquals("59s", formatDuration(59))
    }

    @Test
    fun formatDuration_minutesOnly() {
        assertEquals("1m", formatDuration(60))
        assertEquals("5m", formatDuration(300))
    }

    @Test
    fun formatDuration_minutesAndSeconds() {
        assertEquals("1m 30s", formatDuration(90))
        assertEquals("2m 15s", formatDuration(135))
    }

    @Test
    fun formatDuration_negative() {
        assertEquals("0s", formatDuration(-1))
    }
}
