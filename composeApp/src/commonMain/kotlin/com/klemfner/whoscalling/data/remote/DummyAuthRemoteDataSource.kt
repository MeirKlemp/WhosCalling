package com.klemfner.whoscalling.data.remote

class DummyAuthRemoteDataSource : AuthRemoteDataSource {
    override suspend fun login(username: String, password: String): String {
        if (username.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Username and password must not be blank")
        }
        return "dummy-token-$username"
    }
}
