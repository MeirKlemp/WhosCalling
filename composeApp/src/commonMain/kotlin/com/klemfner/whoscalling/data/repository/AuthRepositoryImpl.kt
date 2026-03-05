package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.AuthLocalDataSource
import com.klemfner.whoscalling.data.remote.AuthRemoteDataSource
import com.klemfner.whoscalling.domain.model.LoggedInUser
import com.klemfner.whoscalling.domain.model.SavedCredentials
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val currentTimeMillis: () -> Long = ::currentTimeMillis,
) : AuthRepository {

    private val _loggedInUser = MutableStateFlow<LoggedInUser?>(null)
    override val loggedInUser: StateFlow<LoggedInUser?> = _loggedInUser.asStateFlow()

    private var token: String? = null
    private var username: String? = null
    private var password: String? = null

    init {
        localDataSource.savedCredentials.value?.let { creds ->
            token = creds.sessionKey
            username = creds.username
            password = creds.password
            _loggedInUser.value = LoggedInUser(creds.username, creds.loginTime)
        }
    }

    override suspend fun login(username: String, password: String, rememberMe: Boolean) {
        val newToken = remoteDataSource.login(username, password)
        val loginTime = currentTimeMillis()
        this.token = newToken
        this.username = username
        this.password = password
        _loggedInUser.value = LoggedInUser(username, loginTime)

        if (rememberMe) {
            localDataSource.saveCredentials(
                SavedCredentials(username, password, loginTime, newToken)
            )
        } else {
            localDataSource.clearCredentials()
        }
    }

    override suspend fun retryLogin() {
        val u = username ?: throw IllegalStateException("No credentials to retry")
        val p = password ?: throw IllegalStateException("No credentials to retry")
        val newToken = remoteDataSource.login(u, p)
        this.token = newToken

        localDataSource.savedCredentials.value?.let { saved ->
            localDataSource.saveCredentials(
                saved.copy(sessionKey = newToken)
            )
        }
    }

    override suspend fun logout() {
        token = null
        username = null
        password = null
        _loggedInUser.value = null
        localDataSource.clearCredentials()
    }

    override fun getToken(): String? = token
}
