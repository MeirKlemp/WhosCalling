package com.klemfner.whoscalling.util

actual fun defaultRouterIp(): String {
    return try {
        val os = System.getProperty("os.name").lowercase()
        when {
            os.contains("win") -> parseWindowsDefaultGateway()
            else -> parseUnixDefaultGateway()
        }
    } catch (_: Exception) {
        ""
    }
}

private fun parseUnixDefaultGateway(): String {
    val process = ProcessBuilder("ip", "route", "show", "default").start()
    try {
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.lines()
            .firstOrNull { it.startsWith("default") }
            ?.split(" ")
            ?.getOrNull(2)
            ?: ""
    } finally {
        process.destroy()
    }
}

private fun parseWindowsDefaultGateway(): String {
    val process = ProcessBuilder("cmd", "/c", "route", "print", "0.0.0.0").start()
    try {
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.lines()
            .firstOrNull { it.trim().startsWith("0.0.0.0") && it.contains("0.0.0.0") }
            ?.trim()
            ?.split("\\s+".toRegex())
            ?.getOrNull(2)
            ?: ""
    } finally {
        process.destroy()
    }
}
