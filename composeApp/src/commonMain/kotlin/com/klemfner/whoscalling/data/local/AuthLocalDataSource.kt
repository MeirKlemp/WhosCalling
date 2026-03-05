package com.klemfner.whoscalling.data.local

interface AuthLocalDataSource {
    fun getSavedUsername(): String?
    fun getSavedPassword(): String?
    fun getSavedToken(): String?
    fun getSavedLoginTime(): Long?
    fun saveCredentials(username: String, password: String, token: String, loginTime: Long)
    fun clearCredentials()
}
