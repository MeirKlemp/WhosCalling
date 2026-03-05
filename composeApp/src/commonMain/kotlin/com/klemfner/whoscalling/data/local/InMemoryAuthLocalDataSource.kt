package com.klemfner.whoscalling.data.local

class InMemoryAuthLocalDataSource : AuthLocalDataSource {
    private var username: String? = null
    private var password: String? = null
    private var token: String? = null
    private var loginTime: Long? = null

    override fun getSavedUsername(): String? = username
    override fun getSavedPassword(): String? = password
    override fun getSavedToken(): String? = token
    override fun getSavedLoginTime(): Long? = loginTime

    override fun saveCredentials(username: String, password: String, token: String, loginTime: Long) {
        this.username = username
        this.password = password
        this.token = token
        this.loginTime = loginTime
    }

    override fun clearCredentials() {
        username = null
        password = null
        token = null
        loginTime = null
    }
}
