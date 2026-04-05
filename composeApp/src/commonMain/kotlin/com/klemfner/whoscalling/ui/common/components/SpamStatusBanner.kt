package com.klemfner.whoscalling.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.detected_as_spam
import whoscalling.composeapp.generated.resources.reported_as_spam
import whoscalling.composeapp.generated.resources.trust_number
import whoscalling.composeapp.generated.resources.trusted_number

@Composable
fun SpamStatusBanner(
    spam: Spam?,
    onReportSafe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (spam == null) return

    val detected = spam.detectedAsSpam
    val reported = spam.reportedAs
    val isSafe = reported == SpamReport.SAFE

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when {
            // Reported as spam (including detected + reported) - show "You reported this number as spam"
            reported == SpamReport.SPAM -> {
                SpamWarningRow(stringResource(Res.string.reported_as_spam))
                TrustNumberRow(onClick = onReportSafe)
            }
            // Detected as spam only (no report)
            detected && !isSafe -> {
                SpamWarningRow(stringResource(Res.string.detected_as_spam))
                TrustNumberRow(onClick = onReportSafe)
            }
            // Detected + safe - show warning in yellow + trusted
            detected && isSafe -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.detected_as_spam),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.trusted_number),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            // Safe only (no detection)
            isSafe -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.trusted_number),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpamWarningRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TrustNumberRow(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.Button,
            )
            .padding(vertical = 4.dp),
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.trust_number),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
