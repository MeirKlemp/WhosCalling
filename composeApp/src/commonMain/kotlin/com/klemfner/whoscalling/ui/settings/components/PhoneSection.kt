package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.ui.common.components.CountryCodeDialog
import com.klemfner.whoscalling.ui.common.components.CountryCodeField
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.country_picker_confirm
import whoscalling.composeapp.generated.resources.country_picker_dismiss
import whoscalling.composeapp.generated.resources.country_picker_title
import whoscalling.composeapp.generated.resources.phone_country_code
import whoscalling.composeapp.generated.resources.reset_to_default

@Composable
fun PhoneSection(
    countryIso: String,
    onCountryIsoChange: (String) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        CountryCodeDialog(
            selectedCountryIso = countryIso,
            onConfirm = { iso ->
                onCountryIsoChange(iso)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            title = stringResource(Res.string.country_picker_title),
            dismissButton = stringResource(Res.string.country_picker_dismiss),
            confirmButton = stringResource(Res.string.country_picker_confirm),
        )
    }

    Text(
        text = stringResource(Res.string.phone_country_code),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    CountryCodeField(
        selectedCountryIso = countryIso,
        onClick = { showDialog = true },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    TextButton(
        onClick = onResetToDefault,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.reset_to_default))
    }
}
