package com.klemfner.whoscalling.domain.model

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null
)
