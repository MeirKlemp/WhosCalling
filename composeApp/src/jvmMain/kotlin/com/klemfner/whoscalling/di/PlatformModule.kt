package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.AuthLocalDataSource
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.DpapiAuthLocalDataSource
import com.klemfner.whoscalling.data.local.FileSettingsLocalDataSource
import com.klemfner.whoscalling.data.local.InMemoryAuthLocalDataSource
import com.klemfner.whoscalling.data.local.JvmDatabaseDriverFactory
import com.klemfner.whoscalling.data.local.SecretToolAuthLocalDataSource
import com.klemfner.whoscalling.data.local.SettingsLocalDataSource
import com.klemfner.whoscalling.data.repository.SettingsRepositoryImpl
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import java.io.File

actual val platformModule = module {
    single<DatabaseDriverFactory> { JvmDatabaseDriverFactory() }
    single<AuthLocalDataSource> { createDesktopAuthLocalDataSource() }
    single<SettingsLocalDataSource> {
        FileSettingsLocalDataSource(settingsFile = desktopSettingsFile())
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            localDataSource = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}

private fun createDesktopAuthLocalDataSource(): AuthLocalDataSource {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> DpapiAuthLocalDataSource()
        os.contains("nux") || os.contains("nix") -> SecretToolAuthLocalDataSource()
        else -> InMemoryAuthLocalDataSource()
    }
}

private fun desktopSettingsFile(): File {
    val os = System.getProperty("os.name").lowercase()
    val appDir = when {
        os.contains("win") -> File(
            System.getenv("APPDATA") ?: System.getProperty("user.home"),
            "WhosCalling",
        )
        else -> File(
            System.getenv("XDG_CONFIG_HOME") ?: "${System.getProperty("user.home")}/.config",
            "WhosCalling",
        )
    }
    return File(appDir, "settings.json")
}
