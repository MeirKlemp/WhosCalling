package com.klemfner.whoscalling.domain.model

data class SavedCredentials(
    val username: String,
    val password: String,
    val loginTime: Long,
    val sessionKey: String,
) {
    override fun toString(): String =
        "SavedCredentials(username=$username, password=***, loginTime=$loginTime, sessionKey=***)"
}
