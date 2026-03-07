package com.klemfner.whoscalling.ui.common.components

import androidx.compose.runtime.Composable
import com.stevdza_san.library.component.CountryPickerDialog
import com.stevdza_san.library.domain.Country
import com.stevdza_san.library.domain.CountryDisplayOption
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.country_picker_confirm
import whoscalling.composeapp.generated.resources.country_picker_dismiss
import whoscalling.composeapp.generated.resources.country_picker_title

@Composable
fun CountryCodeDialog(
    selectedCountryIso: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedCountry = Country.entries.find { it.isoCode == selectedCountryIso }
        ?: Country.UnitedStates
    CountryPickerDialog(
        title = stringResource(Res.string.country_picker_title),
        dismissButton = stringResource(Res.string.country_picker_dismiss),
        confirmButton = stringResource(Res.string.country_picker_confirm),
        displayOption = CountryDisplayOption.DIAL_CODE_AND_NAME,
        showIcon = true,
        selectedCountry = selectedCountry,
        onConfirmClick = { country -> onConfirm(country.isoCode) },
        onDismiss = onDismiss,
    )
}
