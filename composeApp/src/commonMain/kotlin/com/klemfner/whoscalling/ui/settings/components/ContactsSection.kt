package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.contact_count
import whoscalling.composeapp.generated.resources.data
import whoscalling.composeapp.generated.resources.export_data
import whoscalling.composeapp.generated.resources.import_data
import whoscalling.composeapp.generated.resources.no_contacts
import whoscalling.composeapp.generated.resources.no_spam_numbers
import whoscalling.composeapp.generated.resources.spam_count

@Composable
fun ContactsSection(
    contactCount: Int,
    spamCount: Int,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.data),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    Text(
        text = if (contactCount == 0) {
            stringResource(Res.string.no_contacts)
        } else {
            stringResource(Res.string.contact_count, contactCount)
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    Text(
        text = if (spamCount == 0) {
            stringResource(Res.string.no_spam_numbers)
        } else {
            stringResource(Res.string.spam_count, spamCount)
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    TextButton(
        onClick = onExport,
        enabled = contactCount > 0 || spamCount > 0,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.export_data))
    }
    TextButton(
        onClick = onImport,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.import_data))
    }
}
