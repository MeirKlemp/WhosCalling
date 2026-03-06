package com.klemfner.whoscalling.data.remote.srp

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.klemfner.whoscalling.util.secureRandomBytes
import com.klemfner.whoscalling.util.sha256

/**
 * SRP-6a client implementation using the 2048-bit group defined in RFC 5054 with SHA-256.
 *
 * Parameters:
 *  - N : 2048-bit safe prime (RFC 5054 group 14)
 *  - g : 2
 *  - H : SHA-256
 *  - k : H(N | PAD(g)) — the SRP-6a multiplier, also used in M1
 *
 * The pre-calculated H(N || PAD(g)) value is stored as [H_N_CONCAT_G] and used
 * directly in the M1 computation to avoid re-hashing the large prime on every login.
 */
class Srp6aClientImpl : Srp6aClient {

    // -------------------------------------------------------------------------
    // Fixed SRP group parameters
    // -------------------------------------------------------------------------

    private val N: BigInteger = BigInteger.parseString(N_HEX, 16)
    private val g: BigInteger = BigInteger.fromInt(2)

    /** k = H(N | PAD(g)) */
    private val k: BigInteger = run {
        val nBytes = bigIntegerToNBytes(N)
        val gPadded = ByteArray(N_BYTES).also { it[N_BYTES - 1] = 2 }
        BigInteger.fromByteArray(sha256(nBytes + gPadded), Sign.POSITIVE)
    }

    // -------------------------------------------------------------------------
    // Per-exchange state (reset on each generatePublicKey() call)
    // -------------------------------------------------------------------------

    private var privateKey: BigInteger? = null  // a
    private var publicKey: BigInteger? = null   // A = g^a mod N
    private var sessionKey: ByteArray? = null   // K = H(PAD(S))
    private var clientProof: ByteArray? = null  // M1

    // -------------------------------------------------------------------------
    // Srp6aClient implementation
    // -------------------------------------------------------------------------

    override fun generatePublicKey(): String {
        // Reset state for a fresh exchange
        sessionKey = null
        clientProof = null

        // a = random 256-bit private key
        val a = BigInteger.fromByteArray(secureRandomBytes(PRIVATE_KEY_BYTES), Sign.POSITIVE)
        // A = g^a mod N
        val A = g.modPow(a, N)

        privateKey = a
        publicKey = A

        return bytesToHex(bigIntegerToNBytes(A))
    }

    override fun calculateClientProof(
        username: String,
        password: String,
        salt: String,
        serverPublicKey: String,
    ): String {
        val a = privateKey ?: error("generatePublicKey() must be called before calculateClientProof()")
        val A = publicKey ?: error("generatePublicKey() must be called before calculateClientProof()")

        val B = BigInteger.parseString(serverPublicKey, 16)
        val sBytes = hexToBytes(salt)

        // u = H(PAD(A) | PAD(B))
        val u = BigInteger.fromByteArray(
            sha256(bigIntegerToNBytes(A) + bigIntegerToNBytes(B)),
            Sign.POSITIVE,
        )

        // x = H(s | H(I | ":" | P))
        val ipHash = sha256(
            username.encodeToByteArray() +
                ":".encodeToByteArray() +
                password.encodeToByteArray(),
        )
        val x = BigInteger.fromByteArray(sha256(sBytes + ipHash), Sign.POSITIVE)

        // S = (B - k * g^x mod N) ^ (a + u*x) mod N
        val kgx = (k * g.modPow(x, N)).mod(N)
        val diff = (B - kgx).mod(N)
        val S = diff.modPow(a + u * x, N)

        // K = H(PAD(S))
        val K = sha256(bigIntegerToNBytes(S))
        sessionKey = K

        // M1 = H(H(N || PAD(g)) | H(I) | s | PAD(A) | PAD(B) | K)
        val hnConcatG = hexToBytes(H_N_CONCAT_G)
        val hI = sha256(username.encodeToByteArray())
        val M1 = sha256(hnConcatG + hI + sBytes + bigIntegerToNBytes(A) + bigIntegerToNBytes(B) + K)
        clientProof = M1

        return bytesToHex(M1)
    }

    override fun validateServerProof(serverProof: String): Boolean {
        val A = publicKey ?: return false
        val M1 = clientProof ?: return false
        val K = sessionKey ?: return false

        // Expected M2 = H(PAD(A) | M1 | K)
        val expectedM2 = sha256(bigIntegerToNBytes(A) + M1 + K)
        val actualM2 = hexToBytes(serverProof)

        return expectedM2.contentEquals(actualM2)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a [BigInteger] to a big-endian byte array of exactly [N_BYTES] bytes,
     * padding with leading zeros or truncating excess leading bytes as needed.
     */
    internal fun bigIntegerToNBytes(bi: BigInteger): ByteArray {
        val hex = bi.toString(16).let { if (it.length % 2 != 0) "0$it" else it }
        val raw = ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return when {
            raw.size < N_BYTES -> ByteArray(N_BYTES - raw.size) + raw
            raw.size > N_BYTES -> raw.copyOfRange(raw.size - N_BYTES, raw.size)
            else -> raw
        }
    }

    companion object {
        /** Byte length of N (2048 bits / 8 = 256 bytes). */
        const val N_BYTES = 256

        /** Number of bytes for the random SRP private key. */
        private const val PRIVATE_KEY_BYTES = 32

        /**
         * 2048-bit safe prime N from RFC 5054, Group 14.
         * Source: https://www.ietf.org/rfc/rfc5054.txt Appendix A
         */
        const val N_HEX =
            "ac6bdb41324a9a9bf166de5e1389582faf72b6651987ee07fc3192943db56050" +
                "a37329cbb4a099ed8193e0757767a13dd52312ab4b03310dcd7f48a9da04fd5" +
                "0e8083969edb767b0cf6095179a163ab3661a05fbd5faaae82918a9962f0b93" +
                "b855f97993ec975eeaa80d740adbf4ff747359d041d5c33ea71d281e446b147" +
                "73bca97b43a23fb801676bd207a436c6481f1d2b9078717461a5b9d32e688f8" +
                "7748544523b524b0d57d5ea77a2775d2ecfa032cfbdbf52fb3786160279004e" +
                "57ae6af874e7303ce53299ccc041c7bc308d82a5698f3a8d0c38271ae35f8e9" +
                "dbfbb694b5c803d89f7ae435de236d525f54759b65e372fcd68ef20fa7111f9" +
                "e4aff73"

        /**
         * Pre-calculated SHA-256(N | PAD(g)) for the 2048-bit group with g=2.
         * This equals the SRP-6a multiplier k = H(N | PAD(g)) and is used
         * directly in the M1 proof computation as the hash of the group parameters.
         *
         * Note: Some SRP implementations use H(N) XOR H(g) instead; this router
         * uses H(N || PAD(g)) as specified in the problem parameters.
         */
        const val H_N_CONCAT_G = "05b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300"
    }
}

// -------------------------------------------------------------------------
// Package-level helpers (used in tests)
// -------------------------------------------------------------------------

internal fun hexToBytes(hex: String): ByteArray {
    val normalized = if (hex.length % 2 != 0) "0$hex" else hex
    return ByteArray(normalized.length / 2) { i ->
        normalized.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

internal fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder(bytes.size * 2)
    val hexChars = "0123456789abcdef"
    for (b in bytes) {
        val v = b.toInt() and 0xFF
        sb.append(hexChars[v shr 4])
        sb.append(hexChars[v and 0xF])
    }
    return sb.toString()
}
