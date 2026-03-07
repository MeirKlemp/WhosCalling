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
) {
    val selectedCountry = Country.entries.find { it.isoCode == selectedCountryIso }
        ?: Country.UnitedStates
    CountryPickerField(
        modifier = modifier,
        enabled = enabled,
        selectedCountry = selectedCountry,
        displayOption = CountryDisplayOption.DIAL_CODE,
        showIcon = true,
        onClick = onClick,
    )
}
