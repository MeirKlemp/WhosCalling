package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import whoscalling.composeapp.generated.resources.phone_country_code

@Composable
fun PhoneSection(
    countryIso: String,
    onCountryIsoChange: (String) -> Unit,
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
        showCountryName = true,
        onClick = { showDialog = true },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
