package com.klemfner.whoscalling.util

actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    // No-op on JVM desktop, logs are stored internally by Logger
}
