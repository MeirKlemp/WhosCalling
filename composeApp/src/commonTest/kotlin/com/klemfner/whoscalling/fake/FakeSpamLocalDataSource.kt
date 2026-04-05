package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.local.SpamLocalDataSource
import com.klemfner.whoscalling.domain.model.Spam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSpamLocalDataSource : SpamLocalDataSource {
    private val _spams = MutableStateFlow<List<Spam>>(emptyList())
    override val spams: Flow<List<Spam>> = _spams

    override suspend fun getSpam(phoneNumber: String): Spam? {
        return _spams.value.find { it.phoneNumber == phoneNumber }
    }

    override suspend fun saveSpam(spam: Spam) {
        _spams.update { current ->
            current.filterNot { it.phoneNumber == spam.phoneNumber } + spam
        }
    }

    override suspend fun deleteSpam(phoneNumber: String) {
        _spams.update { current -> current.filterNot { it.phoneNumber == phoneNumber } }
    }

    override suspend fun deleteAllSpams() {
        _spams.value = emptyList()
    }
}
