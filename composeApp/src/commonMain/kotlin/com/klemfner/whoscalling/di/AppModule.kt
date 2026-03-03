package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.local.CallLogLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.data.local.ContactLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.data.repository.CallLogRepositoryImpl
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import org.koin.dsl.module

val databaseModule = module {
    single<WhosCallingDatabase> {
        val driverFactory: DatabaseDriverFactory = get()
        WhosCallingDatabase(driverFactory.createDriver())
    }
}

val dataSourceModule = module {
    single<CallLogLocalDataSource> { CallLogLocalDataSourceImpl(get()) }
    single<ContactLocalDataSource> { ContactLocalDataSourceImpl(get()) }
}

val repositoryModule = module {
    single<CallLogRepository> { CallLogRepositoryImpl(get(), get(), get()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
}

val appModules = listOf(databaseModule, dataSourceModule, repositoryModule)
