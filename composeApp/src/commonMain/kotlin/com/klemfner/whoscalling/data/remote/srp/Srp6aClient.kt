package com.klemfner.whoscalling.data.remote.srp

interface Srp6aClient {
    fun generatePublicKey(): String

    fun processChallenge(
        username: String,
        password: String,
        salt: String,
        serverPublicKey: String,
    ): String

    fun verifyServerProof(serverProof: String): Boolean
}
