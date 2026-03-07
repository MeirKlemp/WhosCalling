package com.klemfner.whoscalling.ui.common.components

import androidx.compose.runtime.Composable
import com.stevdza_san.library.component.CountryPickerDialog
import com.stevdza_san.library.domain.Country
import com.stevdza_san.library.domain.CountryDisplayOption

@Composable
fun CountryCodeDialog(
    selectedCountryIso: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Pick a Country",
    dismissButton: String = "Cancel",
    confirmButton: String = "Confirm",
) {
    val selectedCountry = Country.entries.find { it.isoCode == selectedCountryIso }
        ?: Country.UnitedStates
    CountryPickerDialog(
        title = title,
        dismissButton = dismissButton,
        confirmButton = confirmButton,
        displayOption = CountryDisplayOption.DIAL_CODE_AND_NAME,
        showIcon = true,
        selectedCountry = selectedCountry,
        onConfirmClick = { country -> onConfirm(country.isoCode) },
        onDismiss = onDismiss,
    )
}
