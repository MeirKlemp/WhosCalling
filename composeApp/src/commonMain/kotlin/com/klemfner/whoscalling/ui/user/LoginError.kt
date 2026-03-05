package com.klemfner.whoscalling.ui.user

sealed interface LoginError {
    data object BlankCredentials : LoginError
    data object Generic : LoginError
}
