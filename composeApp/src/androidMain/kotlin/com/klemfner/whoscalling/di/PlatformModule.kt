package com.klemfner.whoscalling.di

import android.content.Context
import com.klemfner.whoscalling.data.local.AndroidDatabaseDriverFactory
import com.klemfner.whoscalling.data.local.AuthLocalDataSource
import com.klemfner.whoscalling.data.local.DataStoreSettingsLocalDataSource
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.EncryptedPreferencesAuthLocalDataSource
import com.klemfner.whoscalling.data.local.SettingsLocalDataSource
import com.klemfner.whoscalling.data.repository.SettingsRepositoryImpl
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.util.defaultRouterIp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
    single<AuthLocalDataSource> { EncryptedPreferencesAuthLocalDataSource(get()) }
    single<SettingsLocalDataSource> { DataStoreSettingsLocalDataSource(get()) }
    single<SettingsRepository> {
        val context: Context = get()
        SettingsRepositoryImpl(
            localDataSource = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            defaultRouterIp = { defaultRouterIp(context) },
        )
    }
}
