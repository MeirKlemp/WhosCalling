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
        defaultCountryIso: () -> String = { "US" },
        defaultTouchMode: () -> Boolean = { true },
        scope: CoroutineScope = CoroutineScope(testDispatcher),
    ) = SettingsRepositoryImpl(
        localDataSource = InMemorySettingsLocalDataSource(),
        scope = scope,
        defaultCountryIso = defaultCountryIso,
        defaultTouchMode = defaultTouchMode,
    )

    @Test
    fun preferences_emitsInitialValue() = runTest(testDispatcher) {
        val repository = createRepository(defaultCountryIso = { "US" }, defaultTouchMode = { true })
        repository.preferences.test {
            val initial = awaitItem()
            assertEquals("US", initial.countryIso)
            assertEquals(true, initial.touchMode)
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
        testDispatcher.scheduler.advanceUntilIdle()
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
        val repository = createRepository(defaultCountryIso = { "US" }, defaultTouchMode = { true })
        repository.setCountryIso("IL")
        repository.setTouchMode(false)
        testDispatcher.scheduler.advanceUntilIdle()
        repository.preferences.test {
            // After advancing, stateIn has propagated the updates
            val current = awaitItem()
            assertEquals("IL", current.countryIso)
            assertEquals(false, current.touchMode)

            repository.resetToDefault()
            val reset = awaitItem()
            assertEquals("US", reset.countryIso)
            assertEquals(true, reset.touchMode)

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

    @Test
    fun setRouterIp_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            awaitItem() // initial

            repository.setRouterIp("192.168.1.1")
            assertEquals("192.168.1.1", awaitItem().routerIp)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRouterIp_updatesSyncCurrentRouterIp() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRouterIp("10.0.0.1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("10.0.0.1", repository.currentRouterIp)
    }

    @Test
    fun resetToDefault_restoresRouterIp() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRouterIp("10.0.0.1")
        testDispatcher.scheduler.advanceUntilIdle()
        repository.preferences.test {
            assertEquals("10.0.0.1", awaitItem().routerIp)

            repository.resetToDefault()
            assertEquals("", awaitItem().routerIp)

            cancelAndConsumeRemainingEvents()
        }
    }
}
