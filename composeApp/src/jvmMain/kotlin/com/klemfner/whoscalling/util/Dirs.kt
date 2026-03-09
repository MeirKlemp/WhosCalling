package com.klemfner.whoscalling.util

import java.io.File

private const val APP_NAME = "WhosCalling"

val configDirPath = run {
    val os = System.getProperty("os.name").lowercase()
    val configDir = when {
        os.contains("win") -> System.getenv("APPDATA") ?: System.getProperty("user.home")
        else -> System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config"
    }
    File(configDir, APP_NAME).path
}

val dataDirPath = run {
    val os = System.getProperty("os.name").lowercase()
    val configDir = when {
        os.contains("win") -> System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home")
        else -> System.getenv("XDG_DATA_HOME") ?: "${System.getProperty("user.home")}/.local/share"
    }
    File(configDir, APP_NAME).path
}