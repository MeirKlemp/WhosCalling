package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.SpamLocalDataSource
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import com.klemfner.whoscalling.domain.repository.SpamRepository
import com.klemfner.whoscalling.util.Logger
import com.klemfner.whoscalling.util.currentTimeMillis
import com.klemfner.whoscalling.util.maskPhoneNumber
import kotlinx.coroutines.flow.Flow

class SpamRepositoryImpl(
    private val localDataSource: SpamLocalDataSource,
    private val currentTimeMillis: () -> Long = ::currentTimeMillis,
) : SpamRepository {

    companion object {
        private const val TAG = "SpamRepository"
    }

    override val spams: Flow<List<Spam>> = localDataSource.spams

    override suspend fun getSpam(phoneNumber: String): Spam? {
        return localDataSource.getSpam(phoneNumber)
    }

    override suspend fun reportAsSpam(phoneNumber: String) {
        val existing = localDataSource.getSpam(phoneNumber)
        val now = currentTimeMillis()
        val spam = existing?.copy(
            reportedAs = SpamReport.SPAM,
            reportingTimestamp = now,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            reportedAs = SpamReport.SPAM,
            reportingTimestamp = now,
        )
        localDataSource.saveSpam(spam)
    }

    override suspend fun reportAsSafe(phoneNumber: String) {
        val existing = localDataSource.getSpam(phoneNumber)
        val now = currentTimeMillis()
        val spam = existing?.copy(
            reportedAs = SpamReport.SAFE,
            reportingTimestamp = now,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            reportedAs = SpamReport.SAFE,
            reportingTimestamp = now,
        )
        localDataSource.saveSpam(spam)
    }

    override suspend fun addDetectedSpam(phoneNumber: String) {
        val existing = localDataSource.getSpam(phoneNumber)
        val now = currentTimeMillis()
        val spam = existing?.copy(
            detectedAsSpam = true,
            detectionTimestamp = now,
        ) ?: Spam(
            phoneNumber = phoneNumber,
            detectedAsSpam = true,
            detectionTimestamp = now,
        )
        localDataSource.saveSpam(spam)
    }

    override suspend fun deleteSpam(phoneNumber: String) {
        localDataSource.deleteSpam(phoneNumber)
    }

    override suspend fun addSpams(spams: List<Spam>): Int {
        var imported = 0
        for (spam in spams) {
            try {
                localDataSource.saveSpam(spam)
                imported++
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to import spam: ${maskPhoneNumber(spam.phoneNumber)}", e)
            }
        }
        return imported
    }
}
