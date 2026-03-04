package com.klemfner.whoscalling

import android.app.Application
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WhosCalling : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val file = File(filesDir, "crash_$timestamp.txt")
                file.writeText(throwable.stackTraceToString())
            } catch (_: Throwable) {
                // Ignore errors during crash logging
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
