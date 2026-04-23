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
import com.klemfner.whoscalling.data.remote.PartnerAuthenticationDataSource
import com.klemfner.whoscalling.data.remote.PartnerCallLogDataSource
import com.klemfner.whoscalling.data.remote.srp.Srp6aClient
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl
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
import com.klemfner.whoscalling.ui.calllogs.calllog_details.CallLogDetailsViewModel
import com.klemfner.whoscalling.ui.calllogs.calllogs_list.CallLogsListViewModel
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.contacts.contact_details.ContactDetailsViewModel
import com.klemfner.whoscalling.ui.contacts.contact_form.ContactFormViewModel
import com.klemfner.whoscalling.ui.contacts.contacts_list.ContactsListViewModel
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
    single<CallLogRemoteDataSource> {
        val settingsRepo: SettingsRepository = get()
        PartnerCallLogDataSource(get()) { settingsRepo.currentRouterIp }
    }
    single<HttpClient> { HttpClient() }
    single<Srp6aClient> { Srp6aClientImpl() }
    single<AuthRemoteDataSource> {
        val settingsRepo: SettingsRepository = get()
        PartnerAuthenticationDataSource(get(), get()) { settingsRepo.currentRouterIp }
    }
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
    viewModelOf(::ContactsListViewModel)
    viewModelOf(::ContactDetailsViewModel)
    viewModelOf(::ContactFormViewModel)
    viewModelOf(::CallLogsViewModel)
    viewModelOf(::CallLogsListViewModel)
    viewModelOf(::CallLogDetailsViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::RingingCallViewModel)
}

val appModules = listOf(databaseModule, dataSourceModule, repositoryModule, viewModelModule)
