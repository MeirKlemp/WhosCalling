package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.AuthLocalDataSource
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.DpapiAuthLocalDataSource
import com.klemfner.whoscalling.data.local.InMemoryAuthLocalDataSource
import com.klemfner.whoscalling.data.local.JvmDatabaseDriverFactory
import com.klemfner.whoscalling.data.local.SecretToolAuthLocalDataSource
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { JvmDatabaseDriverFactory() }
    single<AuthLocalDataSource> { createDesktopAuthLocalDataSource() }
}

private fun createDesktopAuthLocalDataSource(): AuthLocalDataSource {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> DpapiAuthLocalDataSource()
        os.contains("nux") || os.contains("nix") -> SecretToolAuthLocalDataSource()
        else -> InMemoryAuthLocalDataSource()
    }
}
