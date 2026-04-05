package com.klemfner.whoscalling.ui.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.cancel
import whoscalling.composeapp.generated.resources.report_spam
import whoscalling.composeapp.generated.resources.report_spam_confirm
import whoscalling.composeapp.generated.resources.trust_number
import whoscalling.composeapp.generated.resources.trust_number_confirm

@Composable
fun ReportSpamDialog(
    phoneNumber: String,
    contactName: String?,
    defaultCountryIso: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val displayName = rememberDisplayName(phoneNumber, contactName, defaultCountryIso)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text(stringResource(Res.string.report_spam)) },
        text = { Text(stringResource(Res.string.report_spam_confirm, displayName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.report_spam))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
fun TrustNumberDialog(
    phoneNumber: String,
    contactName: String?,
    defaultCountryIso: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val displayName = rememberDisplayName(phoneNumber, contactName, defaultCountryIso)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
        title = { Text(stringResource(Res.string.trust_number)) },
        text = { Text(stringResource(Res.string.trust_number_confirm, displayName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.trust_number))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun rememberDisplayName(
    phoneNumber: String,
    contactName: String?,
    defaultCountryIso: String,
): String = remember(phoneNumber, contactName, defaultCountryIso) {
    val formattedPhone = formatPhoneForDisplay(phoneNumber, defaultCountryIso).toString()
    if (contactName != null) "$contactName ($formattedPhone)" else formattedPhone
}
