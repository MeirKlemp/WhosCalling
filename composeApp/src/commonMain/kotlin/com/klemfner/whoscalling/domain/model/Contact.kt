package com.klemfner.whoscalling.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Contact(
    @Transient val id: String = "",
    val name: String,
    val phoneNumber: String,
    val email: String? = null
)
