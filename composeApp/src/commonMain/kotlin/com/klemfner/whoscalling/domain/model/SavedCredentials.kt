package com.klemfner.whoscalling.domain.model

data class SavedCredentials(
    val username: String,
    val password: String,
    val loginTime: Long,
    val sessionKey: String,
)
