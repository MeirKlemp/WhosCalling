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
import com.klemfner.whoscalling.util.configDirPath
import com.klemfner.whoscalling.util.dataDirPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import java.io.File

actual val platformModule = module {
    single<DatabaseDriverFactory> { JvmDatabaseDriverFactory(dbPath = desktopDatabasePath()) }
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

private fun desktopDatabasePath() = File(dataDirPath, "whoscalling.db").path

private fun desktopSettingsFile() = File(configDirPath, "settings.json")
