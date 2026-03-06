package com.klemfner.whoscalling.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFileSaver(): suspend (fileName: String, content: String) -> Boolean

@Composable
expect fun rememberFileLoader(): suspend () -> String?
