package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.local.CallLogLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.data.local.ContactLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.data.remote.AuthRemoteDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.data.remote.PartnerAuthenticationDataSource
import com.klemfner.whoscalling.data.remote.PartnerCallLogsDataSource
import com.klemfner.whoscalling.data.remote.srp.Srp6aClient
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl
import com.klemfner.whoscalling.data.repository.AuthRepositoryImpl
import com.klemfner.whoscalling.data.repository.CallLogRepositoryImpl
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.user.UserViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModelOf
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
    single<CallLogRemoteDataSource> { PartnerCallLogsDataSource(get()) }
    single<HttpClient> { HttpClient() }
    single<Srp6aClient> { Srp6aClientImpl() }
    single<AuthRemoteDataSource> { PartnerAuthenticationDataSource(get(), get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<CallLogRepository> {
        CallLogRepositoryImpl(get(), get(), get(), CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModelOf(::ContactsViewModel)
    viewModelOf(::CallLogsViewModel)
    viewModelOf(::UserViewModel)
}

val appModules = listOf(databaseModule, dataSourceModule, repositoryModule, viewModelModule)
