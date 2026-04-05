package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.data.local.InMemorySettingsLocalDataSource
import com.klemfner.whoscalling.domain.model.ThemeMode
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
        defaultRouterIp: () -> String = { "" },
        scope: CoroutineScope = CoroutineScope(testDispatcher),
    ) = SettingsRepositoryImpl(
        localDataSource = InMemorySettingsLocalDataSource(),
        scope = scope,
        defaultCountryIso = defaultCountryIso,
        defaultTouchMode = defaultTouchMode,
        defaultRouterIp = defaultRouterIp,
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

    @Test
    fun setRefreshRateSeconds_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            awaitItem() // initial

            repository.setRefreshRateSeconds(30L)
            assertEquals(30L, awaitItem().refreshRateSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRefreshRateSeconds_updatesSyncCurrentRefreshRate() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRefreshRateSeconds(60L)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(60L, repository.currentRefreshRateSeconds)
    }

    @Test
    fun resetToDefault_restoresRefreshRate() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRefreshRateSeconds(120L)
        testDispatcher.scheduler.advanceUntilIdle()
        repository.preferences.test {
            assertEquals(120L, awaitItem().refreshRateSeconds)

            repository.resetToDefault()
            assertEquals(5L, awaitItem().refreshRateSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setThemeMode_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            repository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)

            repository.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem().themeMode)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setThemeMode_updatesSyncCurrentThemeMode() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ThemeMode.DARK, repository.currentThemeMode)
    }

    @Test
    fun resetToDefault_restoresThemeMode() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()
        repository.preferences.test {
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)

            repository.resetToDefault()
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRefreshOnStartup_updatesPreferences() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.preferences.test {
            assertEquals(false, awaitItem().refreshOnStartup)

            repository.setRefreshOnStartup(true)
            assertEquals(true, awaitItem().refreshOnStartup)

            repository.setRefreshOnStartup(false)
            assertEquals(false, awaitItem().refreshOnStartup)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRefreshOnStartup_updatesSyncCurrentRefreshOnStartup() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRefreshOnStartup(true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, repository.currentRefreshOnStartup)
    }

    @Test
    fun resetToDefault_restoresRefreshOnStartup() = runTest(testDispatcher) {
        val repository = createRepository()
        repository.setRefreshOnStartup(true)
        testDispatcher.scheduler.advanceUntilIdle()
        repository.preferences.test {
            assertEquals(true, awaitItem().refreshOnStartup)

            repository.resetToDefault()
            assertEquals(false, awaitItem().refreshOnStartup)

            cancelAndConsumeRemainingEvents()
        }
    }
}
