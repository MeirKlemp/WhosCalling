package com.klemfner.whoscalling.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.login
import whoscalling.composeapp.generated.resources.login_error_blank_credentials
import whoscalling.composeapp.generated.resources.login_error_generic
import whoscalling.composeapp.generated.resources.login_time
import whoscalling.composeapp.generated.resources.logout
import whoscalling.composeapp.generated.resources.password
import whoscalling.composeapp.generated.resources.remember_me
import whoscalling.composeapp.generated.resources.user
import whoscalling.composeapp.generated.resources.username

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val blankCredentialsMessage = stringResource(Res.string.login_error_blank_credentials)
    val genericErrorMessage = stringResource(Res.string.login_error_generic)

    LaunchedEffect(uiState.loginError) {
        uiState.loginError?.let { error ->
            val message = when (error) {
                is LoginError.BlankCredentials -> blankCredentialsMessage
                is LoginError.Generic -> genericErrorMessage
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.user)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (uiState.loggedInUser != null) {
                UserProfile(uiState, viewModel)
            } else {
                LoginForm(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: UserUiState,
    viewModel: UserViewModel,
) {
    Column(
        modifier = Modifier.width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.height(72.dp).width(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            label = { Text(stringResource(Res.string.username)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text(stringResource(Res.string.password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.updateRememberMe(!uiState.rememberMe) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = viewModel::updateRememberMe,
            )
            Text(stringResource(Res.string.remember_me))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = viewModel::login,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp).width(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(Res.string.login))
            }
        }
    }
}

@Composable
private fun UserProfile(
    uiState: UserUiState,
    viewModel: UserViewModel,
) {
    val user = uiState.loggedInUser ?: return

    Column(
        modifier = Modifier.width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.height(72.dp).width(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(8.dp))

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.login_time),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${formatShortDate(user.loginTime)} ${formatTimestamp(user.loginTime)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        TextButton(
            onClick = viewModel::logout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.logout))
        }
    }
}
