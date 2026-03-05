package com.klemfner.whoscalling.ui.user

sealed interface LoginError {
    data object BlankCredentials : LoginError
    data class Generic(val message: String?) : LoginError
}
