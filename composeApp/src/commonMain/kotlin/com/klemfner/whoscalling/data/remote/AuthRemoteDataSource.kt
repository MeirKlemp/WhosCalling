package com.klemfner.whoscalling.data.remote

interface AuthRemoteDataSource {
    suspend fun login(username: String, password: String): String
}
