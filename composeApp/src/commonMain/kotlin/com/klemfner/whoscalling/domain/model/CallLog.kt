package com.klemfner.whoscalling.domain.model

data class CallLog(
    val id: String,
    val phoneNumber: String,
    val type: CallType,
    val missed: Boolean,
    val timestamp: Long,
    val duration: Long
)
