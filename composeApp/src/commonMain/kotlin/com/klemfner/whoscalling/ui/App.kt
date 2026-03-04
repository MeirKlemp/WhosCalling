package com.klemfner.whoscalling.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.di.appModules
import com.klemfner.whoscalling.di.platformModule
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.LocalTouchModeState
import com.klemfner.whoscalling.ui.common.utils.TouchModeState
import com.klemfner.whoscalling.ui.common.utils.isPointerInputMode
import com.klemfner.whoscalling.ui.navigation.AppNavigation
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

@Composable
fun App(additionalModules: List<Module> = emptyList()) {
    KoinApplication(application = {
        modules(additionalModules + appModules + platformModule)
    }) {
        AppTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 600.dp
                    var isTouchMode by remember { mutableStateOf(!isPointerInputMode()) }

                    CompositionLocalProvider(
                        LocalIsExpanded provides isExpanded,
                        LocalIsTouchMode provides isTouchMode,
                        LocalTouchModeState provides TouchModeState(
                            isTouchMode = isTouchMode,
                            onTouchModeChange = { isTouchMode = it },
                        ),
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
