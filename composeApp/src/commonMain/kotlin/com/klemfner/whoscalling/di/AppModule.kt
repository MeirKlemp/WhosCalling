package com.klemfner.whoscalling.di

import com.klemfner.whoscalling.data.local.CallLogLocalDataSource
import com.klemfner.whoscalling.data.local.CallLogLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.data.local.ContactLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.DatabaseDriverFactory
import com.klemfner.whoscalling.data.local.SpamLocalDataSource
import com.klemfner.whoscalling.data.local.SpamLocalDataSourceImpl
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.data.remote.AuthRemoteDataSource
import com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource
import com.klemfner.whoscalling.data.remote.DummyAuthRemoteDataSource
import com.klemfner.whoscalling.data.remote.DummyCallLogRemoteDataSource
import com.klemfner.whoscalling.data.repository.AuthRepositoryImpl
import com.klemfner.whoscalling.data.repository.CallLogRepositoryImpl
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.data.repository.SpamRepositoryImpl
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.domain.repository.SpamRepository
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.ringing_banner.RingingCallViewModel
import com.klemfner.whoscalling.ui.settings.SettingsViewModel
import com.klemfner.whoscalling.ui.user.UserViewModel
import com.klemfner.whoscalling.util.normalizePhoneNumber
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
    single<SpamLocalDataSource> { SpamLocalDataSourceImpl(get()) }
    single<CallLogRemoteDataSource> { DummyCallLogRemoteDataSource() }
    single<HttpClient> { HttpClient() }
    single<AuthRemoteDataSource> { DummyAuthRemoteDataSource() }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<CallLogRepository> {
        val settingsRepo: SettingsRepository = get()
        CallLogRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            authRepository = get(),
            settingsRepository = settingsRepo,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            normalizePhone = { phone -> normalizePhoneNumber(phone, settingsRepo.currentCountryIso) },
        )
    }
    single<ContactRepository> {
        val settingsRepo: SettingsRepository = get()
        ContactRepositoryImpl(
            localDataSource = get(),
            normalizePhone = { phone -> normalizePhoneNumber(phone, settingsRepo.currentCountryIso) },
        )
    }
    single<SpamRepository> { SpamRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModelOf(::ContactsViewModel)
    viewModelOf(::CallLogsViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::RingingCallViewModel)
}

val appModules = listOf(databaseModule, dataSourceModule, repositoryModule, viewModelModule)
