package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.data.local.InMemorySettingsLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun createRepository(
        defaultIso: String = "US",
        scope: CoroutineScope = CoroutineScope(testDispatcher),
    ) = SettingsRepositoryImpl(
        localDataSource = InMemorySettingsLocalDataSource(defaultIso),
        defaultIso = defaultIso,
        scope = scope,
    )

    @Test
    fun countryIso_defaultValue() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "IL")
        repository.countryIso.test {
            assertEquals("IL", awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun currentCountryIso_returnsCurrentValue() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "IL")
        assertEquals("IL", repository.currentCountryIso)
    }

    @Test
    fun setCountryIso_updatesFlow() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "US")
        repository.countryIso.test {
            assertEquals("US", awaitItem())

            repository.setCountryIso("IL")
            assertEquals("IL", awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setCountryIso_updatesCurrentCountryIso() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "US")
        repository.setCountryIso("IL")
        assertEquals("IL", repository.currentCountryIso)
    }

    @Test
    fun resetToDefault_restoresDefaultIso() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "US")
        repository.setCountryIso("IL")
        repository.countryIso.test {
            assertEquals("IL", awaitItem())

            repository.resetToDefault()
            assertEquals("US", awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun multipleChanges_emitsEach() = runTest(testDispatcher) {
        val repository = createRepository(defaultIso = "US")
        repository.countryIso.test {
            assertEquals("US", awaitItem())

            repository.setCountryIso("IL")
            assertEquals("IL", awaitItem())

            repository.setCountryIso("DE")
            assertEquals("DE", awaitItem())

            repository.resetToDefault()
            assertEquals("US", awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
