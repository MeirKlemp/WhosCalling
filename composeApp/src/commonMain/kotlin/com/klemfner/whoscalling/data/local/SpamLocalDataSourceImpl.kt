package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SpamLocalDataSourceImpl(
    private val database: WhosCallingDatabase
) : SpamLocalDataSource {

    override val spams: Flow<List<Spam>> =
        database.spamEntityQueries
            .getAllSpams()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    Spam(
                        phoneNumber = entity.phoneNumber,
                        detectedAsSpam = entity.detectedAsSpam != 0L,
                        reportedAs = entity.reportedAs?.let { SpamReport.valueOf(it) },
                        detectionTimestamp = entity.detectionTimestamp,
                        reportingTimestamp = entity.reportingTimestamp,
                    )
                }
            }

    override suspend fun getSpam(phoneNumber: String): Spam? {
        return withContext(Dispatchers.Default) {
            database.spamEntityQueries.getSpamByPhoneNumber(phoneNumber).executeAsOneOrNull()?.let { entity ->
                Spam(
                    phoneNumber = entity.phoneNumber,
                    detectedAsSpam = entity.detectedAsSpam != 0L,
                    reportedAs = entity.reportedAs?.let { SpamReport.valueOf(it) },
                    detectionTimestamp = entity.detectionTimestamp,
                    reportingTimestamp = entity.reportingTimestamp,
                )
            }
        }
    }

    override suspend fun saveSpam(spam: Spam) {
        withContext(Dispatchers.Default) {
            database.spamEntityQueries.insertSpam(
                phoneNumber = spam.phoneNumber,
                detectedAsSpam = if (spam.detectedAsSpam) 1L else 0L,
                reportedAs = spam.reportedAs?.name,
                detectionTimestamp = spam.detectionTimestamp,
                reportingTimestamp = spam.reportingTimestamp,
            )
        }
    }

    override suspend fun deleteSpam(phoneNumber: String) {
        withContext(Dispatchers.Default) {
            database.spamEntityQueries.deleteSpam(phoneNumber)
        }
    }

    override suspend fun deleteAllSpams() {
        withContext(Dispatchers.Default) {
            database.spamEntityQueries.deleteAllSpams()
        }
    }
}
