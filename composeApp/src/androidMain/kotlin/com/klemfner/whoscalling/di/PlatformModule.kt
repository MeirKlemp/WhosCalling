package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.AndroidDatabaseDriverFactory
import com.klemfner.whoscalling.data.local.AuthLocalDataSource
import com.klemfner.whoscalling.data.local.DataStoreSettingsLocalDataSource
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.EncryptedPreferencesAuthLocalDataSource
import com.klemfner.whoscalling.data.local.SettingsLocalDataSource
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
    single<AuthLocalDataSource> { EncryptedPreferencesAuthLocalDataSource(get()) }
    single<SettingsLocalDataSource> { DataStoreSettingsLocalDataSource(get()) }
}
