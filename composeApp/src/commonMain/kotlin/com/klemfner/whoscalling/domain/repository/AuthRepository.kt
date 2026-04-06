package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.LoggedInUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val loggedInUser: StateFlow<LoggedInUser?>
    suspend fun login(username: String, password: String, rememberMe: Boolean)
    suspend fun retryLogin()
    suspend fun logout()
    fun getToken(): String?
}
