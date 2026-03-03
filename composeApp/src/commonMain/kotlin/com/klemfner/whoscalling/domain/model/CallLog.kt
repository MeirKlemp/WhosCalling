package com.klemfner.whoscalling.domain.model

data class CallLog(
    val id: String,
    val phoneNumber: String,
    val contactName: String?,
    val type: CallType,
    val timestamp: Long,
    val duration: Long
)
