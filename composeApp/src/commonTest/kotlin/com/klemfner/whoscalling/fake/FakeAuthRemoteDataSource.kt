package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.remote.AuthRemoteDataSource

class FakeAuthRemoteDataSource : AuthRemoteDataSource {
    private var result: Result<String> = Result.success("fake-token")

    fun setResult(result: Result<String>) {
        this.result = result
    }

    override suspend fun login(username: String, password: String): String {
        return result.getOrThrow()
    }
}
