package com.klemfner.whoscalling.ui.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.cancel
import whoscalling.composeapp.generated.resources.confirm_delete_multiple
import whoscalling.composeapp.generated.resources.confirm_delete_single
import whoscalling.composeapp.generated.resources.delete
import whoscalling.composeapp.generated.resources.delete_contact

@Composable
fun ConfirmDeleteDialog(
    contactName: String?,
    deleteCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
        title = { Text(stringResource(Res.string.delete_contact)) },
        text = {
            Text(
                if (contactName != null) {
                    stringResource(Res.string.confirm_delete_single, contactName)
                } else {
                    stringResource(Res.string.confirm_delete_multiple, deleteCount)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
