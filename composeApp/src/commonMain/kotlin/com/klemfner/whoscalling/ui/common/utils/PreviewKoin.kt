package com.klemfner.whoscalling.ui.common.utils

import androidx.compose.runtime.Composable
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.LoggedInUser
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.CallLogRepository
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.calllogs.calllog_details.CallLogDetailsViewModel
import com.klemfner.whoscalling.ui.calllogs.calllogs_list.CallLogsListViewModel
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.contacts.contact_details.ContactDetailsViewModel
import com.klemfner.whoscalling.ui.contacts.contact_form.ContactFormViewModel
import com.klemfner.whoscalling.ui.contacts.contacts_list.ContactsListViewModel
import com.klemfner.whoscalling.ui.settings.SettingsViewModel
import com.klemfner.whoscalling.ui.user.UserViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private class PreviewContactRepository : ContactRepository {
    override val contacts: Flow<List<Contact>> = MutableStateFlow(previewContacts)
    override suspend fun addContact(contact: Contact) {}
    override suspend fun addContacts(contacts: List<Contact>): Int = contacts.size
    override suspend fun deleteContact(contactId: String) {}
}

private class PreviewCallLogRepository : CallLogRepository {
    override val callLogs: Flow<List<CallLog>> = MutableStateFlow(previewCallLogs)
    override val ringingCall: Flow<CallLog?> = flowOf(null)
    override suspend fun refreshCallLogs() {}
}

private class PreviewAuthRepository : AuthRepository {
    override val loggedInUser: StateFlow<LoggedInUser?> = MutableStateFlow(null)
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
                viewModelOf(::ContactsListViewModel)
                viewModelOf(::ContactDetailsViewModel)
                viewModelOf(::ContactFormViewModel)
                viewModelOf(::CallLogsViewModel)
                viewModelOf(::CallLogsListViewModel)
                viewModelOf(::CallLogDetailsViewModel)
                viewModelOf(::UserViewModel)
                viewModelOf(::SettingsViewModel)
            },
        )
    }) {
        content()
    }
}
