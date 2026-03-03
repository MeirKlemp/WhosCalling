package com.klemfner.whoscalling.domain.model

data class IncomingCall(
    val phoneNumber: String,
    val contactName: String?,
    val timestamp: Long
)
