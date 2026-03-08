package com.klemfner.whoscalling.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.di.appModules
import com.klemfner.whoscalling.di.platformModule
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.LocalTouchModeState
import com.klemfner.whoscalling.ui.common.utils.TouchModeState
import com.klemfner.whoscalling.ui.navigation.AppNavigation
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

@Composable
fun App(additionalModules: List<Module> = emptyList()) {
    KoinApplication(application = {
        modules(additionalModules + appModules + platformModule)
    }) {
        val settingsRepository: SettingsRepository = koinInject()
        val preferences by settingsRepository.preferences.collectAsStateWithLifecycle()

        AppTheme(themeMode = preferences.themeMode) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 600.dp
                    val isTouchMode = preferences.touchMode
                    val scope = rememberCoroutineScope()

                    CompositionLocalProvider(
                        LocalIsExpanded provides isExpanded,
                        LocalIsTouchMode provides isTouchMode,
                        LocalTouchModeState provides TouchModeState(
                            isTouchMode = isTouchMode,
                            setTouchMode = { enabled ->
                                scope.launch { settingsRepository.setTouchMode(enabled) }
                            },
                        ),
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
