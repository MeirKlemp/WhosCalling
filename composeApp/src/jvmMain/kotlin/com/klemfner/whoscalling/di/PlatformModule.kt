package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.JvmDatabaseDriverFactory
import org.koin.dsl.module

val platformModule = module {
    single<DatabaseDriverFactory> { JvmDatabaseDriverFactory() }
}
