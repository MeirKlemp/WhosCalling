package com.klemfner.whoscalling.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null
)
