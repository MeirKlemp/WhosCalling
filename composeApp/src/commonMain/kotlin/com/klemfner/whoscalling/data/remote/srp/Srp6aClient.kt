package com.klemfner.whoscalling.data.remote.srp

/**
 * Interface for a stateful SRP-6a client.
 *
 * Usage per authentication attempt:
 * 1. Call [generatePublicKey] to produce the client public key A.
 * 2. Call [calculateClientProof] with the server's salt and public key to compute M1.
 * 3. Call [validateServerProof] to verify the server's proof M2.
 *
 * Calling [generatePublicKey] again resets internal state for a fresh exchange.
 */
interface Srp6aClient {

    /**
     * Generates a fresh client public key A for a new SRP-6a exchange.
     * Resets all internal state so this instance can be reused across login attempts.
     *
     * @return hex-encoded client public key A
     */
    fun generatePublicKey(): String

    /**
     * Calculates the client proof M1 from the server's challenge.
     * Must be called after [generatePublicKey].
     *
     * @param username the username (I)
     * @param password the plaintext password (P)
     * @param salt hex-encoded salt received from the server (s)
     * @param serverPublicKey hex-encoded server public key received from the server (B)
     * @return hex-encoded client proof M1
     */
    fun calculateClientProof(
        username: String,
        password: String,
        salt: String,
        serverPublicKey: String,
    ): String

    /**
     * Validates the server proof M2 to confirm mutual authentication.
     * Must be called after [calculateClientProof].
     *
     * @param serverProof hex-encoded server proof M2
     * @return `true` if the server proof is valid, `false` otherwise
     */
    fun validateServerProof(serverProof: String): Boolean
}
