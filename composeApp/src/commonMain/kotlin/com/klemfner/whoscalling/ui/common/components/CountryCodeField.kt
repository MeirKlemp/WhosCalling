package com.klemfner.whoscalling.ui.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stevdza_san.library.component.CountryPickerField
import com.stevdza_san.library.domain.Country
import com.stevdza_san.library.domain.CountryDisplayOption

@Composable
fun CountryCodeField(
    selectedCountryIso: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCountryName: Boolean = false,
) {
    val selectedCountry = Country.entries.find { it.isoCode == selectedCountryIso }
        ?: Country.UnitedStates
    val displayOption =
        if (showCountryName) CountryDisplayOption.DIAL_CODE_AND_NAME
        else CountryDisplayOption.DIAL_CODE

    CountryPickerField(
        modifier = modifier,
        enabled = enabled,
        selectedCountry = selectedCountry,
        displayOption = displayOption,
        showIcon = true,
        onClick = onClick,
    )
}
