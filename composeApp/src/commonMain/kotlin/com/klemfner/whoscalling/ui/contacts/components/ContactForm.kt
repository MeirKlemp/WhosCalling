package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.ui.common.components.CountryCodeDialog
import com.klemfner.whoscalling.ui.common.components.CountryCodeField
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.contacts.ContactFormState
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.cancel
import whoscalling.composeapp.generated.resources.close
import whoscalling.composeapp.generated.resources.country_picker_confirm
import whoscalling.composeapp.generated.resources.country_picker_dismiss
import whoscalling.composeapp.generated.resources.country_picker_title
import whoscalling.composeapp.generated.resources.edit_contact
import whoscalling.composeapp.generated.resources.email
import whoscalling.composeapp.generated.resources.name
import whoscalling.composeapp.generated.resources.phone_number
import whoscalling.composeapp.generated.resources.save

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactForm(
    formState: ContactFormState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCountryIsoChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isTouchMode = LocalIsTouchMode.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCountryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorDismiss()
        }
    }

    if (showCountryDialog) {
        CountryCodeDialog(
            selectedCountryIso = formState.selectedCountryIso,
            onConfirm = { iso ->
                onCountryIsoChange(iso)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false },
            title = stringResource(Res.string.country_picker_title),
            dismissButton = stringResource(Res.string.country_picker_dismiss),
            confirmButton = stringResource(Res.string.country_picker_confirm),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (formState.isNew) stringResource(Res.string.add_contact)
                        else stringResource(Res.string.edit_contact),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(Res.string.close),
                        )
                    }
                },
                actions = {
                    if (isTouchMode) {
                        TextButton(onClick = onSave) {
                            Text(stringResource(Res.string.save))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                CountryCodeField(
                    selectedCountryIso = formState.selectedCountryIso,
                    onClick = { showCountryDialog = true },
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = formState.phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(Res.string.phone_number)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(Res.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (!isTouchMode) {
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        }
    }
}
