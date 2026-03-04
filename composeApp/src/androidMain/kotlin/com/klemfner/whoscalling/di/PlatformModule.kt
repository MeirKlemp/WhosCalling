package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.AndroidDatabaseDriverFactory
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
}
