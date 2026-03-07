package com.klemfner.whoscalling.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val countryIso: String = "",
    val touchMode: Boolean = true,
)
