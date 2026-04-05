package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import com.klemfner.whoscalling.domain.repository.SpamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSpamRepository(
    initialSpams: List<Spam> = emptyList()
) : SpamRepository {

    private val _spams = MutableStateFlow(initialSpams)
    override val spams: Flow<List<Spam>> = _spams.asStateFlow()

    override suspend fun getSpam(phoneNumber: String): Spam? {
        return _spams.value.find { it.phoneNumber == phoneNumber }
    }

    override suspend fun reportAsSpam(phoneNumber: String) {
        val existing = _spams.value.find { it.phoneNumber == phoneNumber }
        val spam = existing?.copy(
            reportedAs = SpamReport.SPAM,
            reportingTimestamp = 0L,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            reportedAs = SpamReport.SPAM,
            reportingTimestamp = 0L,
        )
        _spams.value = _spams.value.filterNot { it.phoneNumber == phoneNumber } + spam
    }

    override suspend fun reportAsSafe(phoneNumber: String) {
        val existing = _spams.value.find { it.phoneNumber == phoneNumber }
        val spam = existing?.copy(
            reportedAs = SpamReport.SAFE,
            reportingTimestamp = 0L,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            reportedAs = SpamReport.SAFE,
            reportingTimestamp = 0L,
        )
        _spams.value = _spams.value.filterNot { it.phoneNumber == phoneNumber } + spam
    }

    override suspend fun addDetectedSpam(phoneNumber: String) {
        val existing = _spams.value.find { it.phoneNumber == phoneNumber }
        val spam = existing?.copy(
            detectedAsSpam = true,
            detectionTimestamp = 0L,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            detectedAsSpam = true,
            detectionTimestamp = 0L,
        )
        _spams.value = _spams.value.filterNot { it.phoneNumber == phoneNumber } + spam
    }

    override suspend fun deleteSpam(phoneNumber: String) {
        _spams.value = _spams.value.filterNot { it.phoneNumber == phoneNumber }
    }

    override suspend fun addSpams(spams: List<Spam>): Int {
        _spams.value = _spams.value + spams
        return spams.size
    }

    // For testing: Mutators
    fun setSpams(spams: List<Spam>) {
        _spams.value = spams
    }

    fun clear() {
        _spams.value = emptyList()
    }
}
