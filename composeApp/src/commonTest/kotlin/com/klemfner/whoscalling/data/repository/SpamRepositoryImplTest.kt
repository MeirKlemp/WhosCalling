package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import com.klemfner.whoscalling.fake.FakeSpamLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpamRepositoryImplTest {

    private val localDataSource = FakeSpamLocalDataSource()
    private var currentTime = 1000L
    private val repository = SpamRepositoryImpl(localDataSource) { currentTime }

    @Test
    fun initiallyNoSpams() = runTest {
        repository.spams.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun reportAsSpam_createsNewEntry() = runTest {
        repository.reportAsSpam("+1234567890")

        val spam = repository.getSpam("+1234567890")
        assertNotNull(spam)
        assertEquals(SpamReport.SPAM, spam.reportedAs)
        assertEquals(1000L, spam.reportingTimestamp)
        assertTrue(spam.isSpam)
    }

    @Test
    fun reportAsSafe_createsNewEntry() = runTest {
        repository.reportAsSafe("+1234567890")

        val spam = repository.getSpam("+1234567890")
        assertNotNull(spam)
        assertEquals(SpamReport.SAFE, spam.reportedAs)
        assertFalse(spam.isSpam)
    }

    @Test
    fun reportAsSafe_overridesDetectedSpam() = runTest {
        repository.addDetectedSpam("+1234567890")
        var spam = repository.getSpam("+1234567890")
        assertNotNull(spam)
        assertTrue(spam.isSpam)

        currentTime = 2000L
        repository.reportAsSafe("+1234567890")
        spam = repository.getSpam("+1234567890")
        assertNotNull(spam)
        assertTrue(spam.detectedAsSpam)
        assertEquals(SpamReport.SAFE, spam.reportedAs)
        assertFalse(spam.isSpam)
    }

    @Test
    fun addDetectedSpam_createsNewEntry() = runTest {
        repository.addDetectedSpam("+1234567890")

        val spam = repository.getSpam("+1234567890")
        assertNotNull(spam)
        assertTrue(spam.detectedAsSpam)
        assertTrue(spam.isSpam)
    }

    @Test
    fun deleteSpam_removesEntry() = runTest {
        repository.reportAsSpam("+1234567890")
        assertNotNull(repository.getSpam("+1234567890"))

        repository.deleteSpam("+1234567890")
        assertNull(repository.getSpam("+1234567890"))
    }

    @Test
    fun addSpams_returnsCount() = runTest {
        val spams = listOf(
            Spam(phoneNumber = "+1111111111", detectedAsSpam = true),
            Spam(phoneNumber = "+2222222222", reportedAs = SpamReport.SPAM),
        )
        val count = repository.addSpams(spams)
        assertEquals(2, count)
    }

    @Test
    fun reportSpamThenSafe_clearesSpamStatus() = runTest {
        repository.reportAsSpam("+1234567890")
        assertTrue(repository.getSpam("+1234567890")!!.isSpam)

        currentTime = 2000L
        repository.reportAsSafe("+1234567890")
        assertFalse(repository.getSpam("+1234567890")!!.isSpam)
    }

    @Test
    fun spamFlowEmitsUpdates() = runTest {
        repository.spams.test {
            assertEquals(emptyList(), awaitItem())

            repository.reportAsSpam("+1234567890")
            val spams = awaitItem()
            assertEquals(1, spams.size)
            assertEquals("+1234567890", spams[0].phoneNumber)

            cancelAndConsumeRemainingEvents()
        }
    }
}
