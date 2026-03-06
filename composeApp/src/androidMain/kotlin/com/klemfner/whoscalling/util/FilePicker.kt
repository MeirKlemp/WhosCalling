package com.klemfner.whoscalling.util

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

@Composable
actual fun rememberFileSaver(): suspend (fileName: String, content: String) -> Unit {
    val context = LocalContext.current
    var pendingContent by remember { mutableStateOf<String?>(null) }
    var saveDeferred by remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(pendingContent?.toByteArray(Charsets.UTF_8) ?: return@use)
            }
        }
        pendingContent = null
        saveDeferred?.complete(Unit)
        saveDeferred = null
    }

    return { fileName, content ->
        pendingContent = content
        val deferred = CompletableDeferred<Unit>()
        saveDeferred = deferred
        launcher.launch(fileName)
        deferred.await()
    }
}

@Composable
actual fun rememberFileLoader(): suspend () -> String? {
    val context = LocalContext.current
    var loadDeferred by remember { mutableStateOf<CompletableDeferred<String?>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val result = uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            }
        }
        loadDeferred?.complete(result)
        loadDeferred = null
    }

    return {
        val deferred = CompletableDeferred<String?>()
        loadDeferred = deferred
        launcher.launch(arrayOf("application/json"))
        deferred.await()
    }
}
