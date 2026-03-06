package com.klemfner.whoscalling.data.remote.srp

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.klemfner.whoscalling.util.sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [Srp6aClientImpl].
 *
 * Because SRP-6a involves secret random values, many tests work by simulating
 * the server side with known inputs and verifying that the client and server
 * converge on the same session key / proofs.
 */
class Srp6aClientImplTest {

    private val client = Srp6aClientImpl()

    // -------------------------------------------------------------------------
    // Helper: server-side SRP-6a computation (mirrors the router)
    // -------------------------------------------------------------------------

    private val N: BigInteger = BigInteger.parseString(Srp6aClientImpl.N_HEX, 16)
    private val g: BigInteger = BigInteger.fromInt(2)
    private val k: BigInteger = run {
        val nBytes = client.bigIntegerToNBytes(N)
        val gPadded = ByteArray(Srp6aClientImpl.N_BYTES).also { it[Srp6aClientImpl.N_BYTES - 1] = 2 }
        BigInteger.fromByteArray(sha256(nBytes + gPadded), Sign.POSITIVE)
    }

    /**
     * Computes the SRP verifier: v = g^x mod N.
     * x = H(s | H(I | ":" | P))
     */
    private fun computeVerifier(username: String, password: String, salt: ByteArray): BigInteger {
        val ipHash = sha256(
            username.encodeToByteArray() +
                ":".encodeToByteArray() +
                password.encodeToByteArray(),
        )
        val x = BigInteger.fromByteArray(sha256(salt + ipHash), Sign.POSITIVE)
        return g.modPow(x, N)
    }

    /**
     * Simulates the server's B computation: B = k*v + g^b mod N.
     */
    private fun computeServerPublicKey(v: BigInteger, b: BigInteger): BigInteger =
        (k * v + g.modPow(b, N)).mod(N)

    /**
     * Simulates the server's session key computation.
     * S_server = (A * v^u)^b mod N
     * K_server = H(PAD(S_server))
     */
    private fun computeServerSessionKey(
        A: BigInteger,
        B: BigInteger,
        v: BigInteger,
        b: BigInteger,
    ): ByteArray {
        val u = BigInteger.fromByteArray(
            sha256(client.bigIntegerToNBytes(A) + client.bigIntegerToNBytes(B)),
            Sign.POSITIVE,
        )
        val S = (A * v.modPow(u, N)).modPow(b, N)
        return sha256(client.bigIntegerToNBytes(S))
    }

    /**
     * Simulates the server's M2 proof computation.
     * M2 = H(PAD(A) | M1 | K_server)
     */
    private fun computeServerM2(A: BigInteger, M1: ByteArray, K: ByteArray): ByteArray =
        sha256(client.bigIntegerToNBytes(A) + M1 + K)

    /**
     * Simulates the server's verification of M1.
     * Expected M1 = H(H(N || PAD(g)) | H(I) | s | PAD(A) | PAD(B) | K_server)
     */
    private fun computeServerExpectedM1(
        username: String,
        salt: ByteArray,
        A: BigInteger,
        B: BigInteger,
        K: ByteArray,
    ): ByteArray {
        val hnConcatG = hexToBytes(Srp6aClientImpl.H_N_CONCAT_G)
        val hI = sha256(username.encodeToByteArray())
        return sha256(hnConcatG + hI + salt + client.bigIntegerToNBytes(A) + client.bigIntegerToNBytes(B) + K)
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    fun generatePublicKeyReturnsNonEmptyHexString() {
        val publicKey = client.generatePublicKey()
        assertTrue(publicKey.isNotBlank(), "Public key should not be blank")
        assertTrue(publicKey.all { it in "0123456789abcdef" }, "Public key should be hex")
    }

    @Test
    fun generatePublicKeyReturnsValidGroupElement() {
        val publicKeyHex = client.generatePublicKey()
        val A = BigInteger.parseString(publicKeyHex, 16)
        // A must be in the range [2, N-1]
        assertTrue(A > BigInteger.ONE, "A must be > 1")
        assertTrue(A < N, "A must be < N")
    }

    @Test
    fun generatePublicKeyProducesUniqueValues() {
        val key1 = client.generatePublicKey()
        val key2 = client.generatePublicKey()
        // Probability of collision is astronomically low with 256-bit random keys
        assertTrue(key1 != key2, "Two consecutive public keys should differ")
    }

    @Test
    fun fullRoundTripWithSimulatedServer() {
        val username = "admin"
        val password = "secret"
        val saltHex = "beb25379d1a8581eb5a727673a2441ee"
        val saltBytes = hexToBytes(saltHex)

        // Server computes verifier and private key
        val v = computeVerifier(username, password, saltBytes)
        val serverPrivateKey = BigInteger.parseString(
            "e487cb59d31ac550471e81f00f6928e01dda08e974a004f49e61f5d105284d20",
            16,
        )

        // Client generates public key A
        val publicKeyHex = client.generatePublicKey()
        val A = BigInteger.parseString(publicKeyHex, 16)

        // Server computes B
        val B = computeServerPublicKey(v, serverPrivateKey)
        val serverPublicKeyHex = bytesToHex(client.bigIntegerToNBytes(B))

        // Client calculates M1
        val clientM1Hex = client.calculateClientProof(
            username = username,
            password = password,
            salt = saltHex,
            serverPublicKey = serverPublicKeyHex,
        )
        val clientM1 = hexToBytes(clientM1Hex)

        // Server computes its own session key
        val serverK = computeServerSessionKey(A, B, v, serverPrivateKey)

        // Server verifies the client's M1
        val serverExpectedM1 = computeServerExpectedM1(username, saltBytes, A, B, serverK)
        assertTrue(
            serverExpectedM1.contentEquals(clientM1),
            "Server's expected M1 must match client's M1",
        )

        // Server computes M2
        val serverM2 = computeServerM2(A, clientM1, serverK)
        val serverM2Hex = bytesToHex(serverM2)

        // Client validates server M2
        assertTrue(
            client.validateServerProof(serverM2Hex),
            "Client must accept valid server proof M2",
        )
    }

    @Test
    fun validateServerProofRejectsWrongProof() {
        val username = "admin"
        val password = "secret"
        val saltHex = "beb25379d1a8581eb5a727673a2441ee"
        val saltBytes = hexToBytes(saltHex)

        val v = computeVerifier(username, password, saltBytes)
        val serverPrivateKey = BigInteger.parseString(
            "e487cb59d31ac550471e81f00f6928e01dda08e974a004f49e61f5d105284d20",
            16,
        )

        client.generatePublicKey()
        val B = computeServerPublicKey(v, serverPrivateKey)

        client.calculateClientProof(
            username = username,
            password = password,
            salt = saltHex,
            serverPublicKey = bytesToHex(client.bigIntegerToNBytes(B)),
        )

        val wrongM2 = "00".repeat(32)
        assertFalse(
            client.validateServerProof(wrongM2),
            "Client must reject an invalid server proof",
        )
    }

    @Test
    fun validateServerProofReturnsFalseBeforeCalculation() {
        val freshClient = Srp6aClientImpl()
        assertFalse(
            freshClient.validateServerProof("00".repeat(32)),
            "validateServerProof should return false when called before calculateClientProof",
        )
    }

    @Test
    fun generatePublicKeyResetsStateForNewExchange() {
        val username = "admin"
        val password = "secret"
        val saltHex = "beb25379d1a8581eb5a727673a2441ee"
        val saltBytes = hexToBytes(saltHex)

        val v = computeVerifier(username, password, saltBytes)
        val serverPrivateKey = BigInteger.parseString(
            "e487cb59d31ac550471e81f00f6928e01dda08e974a004f49e61f5d105284d20",
            16,
        )

        // First exchange
        val firstPublicKeyHex = client.generatePublicKey()
        val firstA = BigInteger.parseString(firstPublicKeyHex, 16)
        val firstB = computeServerPublicKey(v, serverPrivateKey)

        client.calculateClientProof(
            username = username,
            password = password,
            salt = saltHex,
            serverPublicKey = bytesToHex(client.bigIntegerToNBytes(firstB)),
        )

        // Reset via generatePublicKey and do a second exchange
        val secondPublicKeyHex = client.generatePublicKey()
        val secondA = BigInteger.parseString(secondPublicKeyHex, 16)
        val secondB = computeServerPublicKey(v, serverPrivateKey)

        val secondM1Hex = client.calculateClientProof(
            username = username,
            password = password,
            salt = saltHex,
            serverPublicKey = bytesToHex(client.bigIntegerToNBytes(secondB)),
        )
        val secondM1 = hexToBytes(secondM1Hex)

        val serverK = computeServerSessionKey(secondA, secondB, v, serverPrivateKey)
        val serverM2 = computeServerM2(secondA, secondM1, serverK)

        assertTrue(
            client.validateServerProof(bytesToHex(serverM2)),
            "Second exchange should succeed after resetting via generatePublicKey",
        )
    }

    @Test
    fun bigIntegerToNBytesProducesPaddedResult() {
        // A value smaller than 256 bytes should be zero-padded to N_BYTES
        val small = BigInteger.fromInt(1)
        val padded = client.bigIntegerToNBytes(small)
        assertEquals(Srp6aClientImpl.N_BYTES, padded.size)
        // All bytes except the last should be zero
        for (i in 0 until Srp6aClientImpl.N_BYTES - 1) {
            assertEquals(0.toByte(), padded[i], "Leading byte at index $i should be 0")
        }
        assertEquals(1.toByte(), padded[Srp6aClientImpl.N_BYTES - 1])
    }

    @Test
    fun hexToBytesAndBytesToHexRoundTrip() {
        val original = "deadbeefcafebabe"
        val bytes = hexToBytes(original)
        val result = bytesToHex(bytes)
        assertEquals(original, result)
    }

    @Test
    fun hexToBytesHandlesOddLength() {
        // Odd-length hex should be treated as if padded with a leading '0'
        val bytes = hexToBytes("f")
        assertEquals(1, bytes.size)
        assertEquals(0x0f.toByte(), bytes[0])
    }
}
