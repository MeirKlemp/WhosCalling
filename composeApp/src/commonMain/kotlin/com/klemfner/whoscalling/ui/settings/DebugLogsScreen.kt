package com.klemfner.whoscalling.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.utils.PlatformBackHandler
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatShortTime
import com.klemfner.whoscalling.util.LogEntry
import com.klemfner.whoscalling.util.LogLevel
import com.klemfner.whoscalling.util.Logger
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.back
import whoscalling.composeapp.generated.resources.copy
import whoscalling.composeapp.generated.resources.debug_logs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logs by Logger.logs.collectAsStateWithLifecycle()
    val reversedLogs = remember(logs) { logs.reversed() }
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    PlatformBackHandler(enabled = true, onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.debug_logs)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        },
        modifier = modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                onBack()
                true
            } else false
        },
    ) { paddingValues ->
        SelectionContainer {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(reversedLogs) { entry ->
                    LogEntryCard(
                        entry = entry,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(buildLogText(entry)))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    entry: LogEntry,
    onCopy: () -> Unit,
) {
    val cardColor = when (entry.level) {
        LogLevel.VERBOSE -> Color(0xFFE0E0E0)
        LogLevel.DEBUG -> Color(0xFFBBDEFB)
        LogLevel.INFO -> Color(0xFFC8E6C9)
        LogLevel.WARN -> Color(0xFFFFF9C4)
        LogLevel.ERROR -> Color(0xFFFFCDD2)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${entry.level.name} | ${entry.tag}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatShortDate(entry.timestamp)} ${formatShortTime(entry.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.7f),
                    )
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.copy),
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
            entry.throwable?.let { stacktrace ->
                Text(
                    text = stacktrace,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun buildLogText(entry: LogEntry): String = buildString {
    append("${entry.level.name} | ${entry.tag}\n")
    append(entry.message)
    entry.throwable?.let { append("\n$it") }
}
