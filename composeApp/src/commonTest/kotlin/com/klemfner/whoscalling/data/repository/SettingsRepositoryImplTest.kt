package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.data.local.InMemorySettingsLocalDataSource
import com.klemfner.whoscalling.domain.model.UserPreferences
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
        scope: CoroutineScope = CoroutineScope(testDispatcher),
    ) = SettingsRepositoryImpl(
        localDataSource = InMemorySettingsLocalDataSource(),
        scope = scope,
    )

    @Test
    fun preferences_emitsInitialValue() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            val initial = awaitItem()
            assertEquals("", initial.countryIso)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setCountryIso_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            awaitItem() // initial

            repository.setCountryIso("IL")
            assertEquals("IL", awaitItem().countryIso)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setCountryIso_updatesSyncCurrentCountryIso() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setCountryIso("IL")
        assertEquals("IL", repository.currentCountryIso)
    }

    @Test
    fun setTouchMode_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            awaitItem() // initial

            repository.setTouchMode(false)
            assertEquals(false, awaitItem().touchMode)

            repository.setTouchMode(true)
            assertEquals(true, awaitItem().touchMode)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resetToDefault_restoresDefaultValues() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setCountryIso("IL")
        repository.setTouchMode(false)
        repository.preferences.test {
            // After double update, eagerly-started flow emits current value
            val current = awaitItem()
            assertEquals("IL", current.countryIso)
            assertEquals(false, current.touchMode)

            repository.resetToDefault()
            val reset = awaitItem()
            assertEquals("", reset.countryIso)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun multipleChanges_emitsEach() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            awaitItem() // initial

            repository.setCountryIso("IL")
            assertEquals("IL", awaitItem().countryIso)

            repository.setCountryIso("DE")
            assertEquals("DE", awaitItem().countryIso)

            cancelAndConsumeRemainingEvents()
        }
    }
}
