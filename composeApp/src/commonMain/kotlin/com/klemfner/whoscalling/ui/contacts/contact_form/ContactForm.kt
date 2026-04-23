package com.klemfner.whoscalling.ui.contacts.contact_form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.components.CountryCodeDialog
import com.klemfner.whoscalling.ui.common.components.CountryCodeField
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.contacts.ContactFormMode
import com.klemfner.whoscalling.ui.contacts.ContactsError
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.cancel
import whoscalling.composeapp.generated.resources.close
import whoscalling.composeapp.generated.resources.contacts_error_generic_form_error
import whoscalling.composeapp.generated.resources.contacts_error_invalid_phone_number
import whoscalling.composeapp.generated.resources.edit_contact
import whoscalling.composeapp.generated.resources.email
import whoscalling.composeapp.generated.resources.name
import whoscalling.composeapp.generated.resources.phone_number
import whoscalling.composeapp.generated.resources.save

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactForm(
    screenVM: ContactsViewModel,
    modifier: Modifier = Modifier,
    viewModel: ContactFormViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val formState = uiState.formState

    LaunchedEffect(Unit) {
        val state = screenVM.uiState.value
        when (state.formMode) {
            ContactFormMode.NEW -> viewModel.initForNew(state.newContactPhone, state.defaultCountryIso)
            ContactFormMode.EDIT -> state.selectedContact?.let {
                viewModel.initForEdit(it, state.defaultCountryIso)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { event ->
            when (event) {
                ContactFormSaveEvent.SavedNew -> screenVM.onFormSavedNew()
                is ContactFormSaveEvent.SavedEdit -> screenVM.onFormSavedEdit(event.contact)
            }
        }
    }
    val isTouchMode = LocalIsTouchMode.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCountryDialog by remember { mutableStateOf(false) }
    val phoneFocusRequester = remember { FocusRequester() }

    val invalidPhoneNumberErrorMessage = stringResource(Res.string.contacts_error_invalid_phone_number)
    val genericErrorMessage = stringResource(Res.string.contacts_error_generic_form_error)

    LaunchedEffect(uiState.error) {
        val error = uiState.error
        if (error != null) {
            val errorMessage = when (error) {
                is ContactsError.InvalidPhoneNumber -> invalidPhoneNumberErrorMessage
                is ContactsError.GenericFormError -> genericErrorMessage
            }
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    if (showCountryDialog) {
        CountryCodeDialog(
            selectedCountryIso = formState.selectedCountryIso,
            onConfirm = { iso ->
                viewModel.updateFormCountryIso(iso)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false },
        )
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        if (formState.isNew) stringResource(Res.string.add_contact)
                        else stringResource(Res.string.edit_contact),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = screenVM::goBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(Res.string.close),
                        )
                    }
                },
                actions = {
                    if (isTouchMode) {
                        TextButton(onClick = viewModel::saveContact) {
                            Text(stringResource(Res.string.save))
                        }
                    }
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = viewModel::updateFormName,
                    label = { Text(stringResource(Res.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction =
                            if (formState.phoneNumber.isEmpty()) ImeAction.Next
                            else ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { phoneFocusRequester.requestFocus() },
                        onDone = { viewModel.saveContact() },
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CountryCodeField(
                        selectedCountryIso = formState.selectedCountryIso,
                        onClick = { showCountryDialog = true },
                        modifier = Modifier.align(Alignment.Bottom),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = formState.phoneNumber,
                        onValueChange = viewModel::updateFormPhone,
                        label = { Text(stringResource(Res.string.phone_number)) },
                        modifier = Modifier.weight(1f).focusRequester(phoneFocusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.saveContact() }),
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = formState.email,
                    onValueChange = viewModel::updateFormEmail,
                    label = { Text(stringResource(Res.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.saveContact() }),
                )

                if (!isTouchMode) {
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = screenVM::goBack) {
                            Text(stringResource(Res.string.cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = viewModel::saveContact) {
                            Text(stringResource(Res.string.save))
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
