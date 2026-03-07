package com.klemfner.whoscalling.ui.user

import com.klemfner.whoscalling.domain.model.LoggedInUser

data class UserUiState(
    val loggedInUser: LoggedInUser? = null,
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val loginError: LoginError? = null,
    val routerIp: String = "",
)
