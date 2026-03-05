package com.klemfner.whoscalling.ui.common.utils

import androidx.compose.runtime.Composable
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.LoggedInUser
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.user.UserViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private class PreviewContactRepository : ContactRepository {
    override val contacts: Flow<List<Contact>> = MutableStateFlow(previewContacts)
    override suspend fun addContact(contact: Contact) {}
    override suspend fun deleteContact(contactId: String) {}
}

private class PreviewCallLogRepository : CallLogRepository {
    override val callLogs: Flow<List<CallLog>> = MutableStateFlow(previewCallLogs)
    override val incomingCallLog: Flow<CallLog?> = flowOf(null)
    override suspend fun refreshCallLogs() {}
}

private class PreviewAuthRepository : AuthRepository {
    override val loggedInUser: Flow<LoggedInUser?> = flowOf(null)
    override suspend fun login(username: String, password: String, rememberMe: Boolean) {}
    override suspend fun retryLogin() {}
    override suspend fun logout() {}
    override fun getToken(): String? = null
}

@Composable
fun PreviewKoinApplication(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(
            module {
                single<ContactRepository> { PreviewContactRepository() }
                single<CallLogRepository> { PreviewCallLogRepository() }
                single<AuthRepository> { PreviewAuthRepository() }
                viewModelOf(::ContactsViewModel)
                viewModelOf(::CallLogsViewModel)
                viewModelOf(::UserViewModel)
            },
        )
    }) {
        content()
    }
}
