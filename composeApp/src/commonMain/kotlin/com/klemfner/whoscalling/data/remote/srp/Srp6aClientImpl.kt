package com.klemfner.whoscalling.data.remote.srp

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.klemfner.whoscalling.util.secureRandomBytes
import com.klemfner.whoscalling.util.sha256

@OptIn(ExperimentalStdlibApi::class)
class Srp6aClientImpl : Srp6aClient {

    private var privateKey = BigInteger.ZERO
    private var publicKeyA = BigInteger.ZERO
    private var sessionKey = ByteArray(0)
    private var clientProofM = ByteArray(0)

    override fun generatePublicKey(): String {
        privateKey = generateRandomPrivateKey()
        publicKeyA = modPow(G, privateKey, N)
        return publicKeyA.toString(16)
    }

    override fun processChallenge(
        username: String,
        password: String,
        salt: String,
        serverPublicKey: String,
    ): String {
        val B = BigInteger.parseString(serverPublicKey, 16)
        require(B.mod(N) != BigInteger.ZERO) { "Invalid server public key" }

        val u = computeU(publicKeyA, B)
        require(u != BigInteger.ZERO) { "Invalid scrambling parameter" }

        val x = computeX(salt, username, password)
        val S = computeS(B, x, u)
        sessionKey = sha256(bigIntToBytes(S))

        clientProofM = computeM(username, salt, publicKeyA, B, sessionKey)
        return clientProofM.toHexString()
    }

    override fun verifyServerProof(serverProof: String): Boolean {
        val expected = sha256(bigIntToBytes(publicKeyA) + clientProofM + sessionKey)
        return expected.toHexString() == serverProof.lowercase()
    }

    private fun computeS(B: BigInteger, x: BigInteger, u: BigInteger): BigInteger {
        val gx = modPow(G, x, N)
        val kgx = (K * gx).mod(N)
        val diff = (B + N - kgx).mod(N)
        val exp = privateKey + u * x
        return modPow(diff, exp, N)
    }

    companion object {
        internal const val GROUP_SIZE = 256

        val N: BigInteger = BigInteger.parseString(
            "ac6bdb41324a9a9bf166de5e1389582faf72b6651987ee07fc3192943db56050" +
                "a37329cbb4a099ed8193e0757767a13dd52312ab4b03310dcd7f48a9da04fd50" +
                "e8083969edb767b0cf6095179a163ab3661a05fbd5faaae82918a9962f0b93b8" +
                "55f97993ec975eeaa80d740adbf4ff747359d041d5c33ea71d281e446b14773b" +
                "ca97b43a23fb801676bd207a436c6481f1d2b9078717461a5b9d32e688f87748" +
                "544523b524b0d57d5ea77a2775d2ecfa032cfbdbf52fb3786160279004e57ae6" +
                "af874e7303ce53299ccc041c7bc308d82a5698f3a8d0c38271ae35f8e9dbfbb6" +
                "94b5c803d89f7ae435de236d525f54759b65e372fcd68ef20fa7111f9e4aff73",
            16,
        )

        val G: BigInteger = BigInteger(2)

        val K: BigInteger = BigInteger.parseString(
            "05b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300",
            16,
        )

        private val TWO = BigInteger(2)

        private fun generateRandomPrivateKey(): BigInteger {
            val bytes = secureRandomBytes(GROUP_SIZE)
            return BigInteger.fromByteArray(bytes, Sign.POSITIVE).mod(N)
        }

        internal fun modPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
            if (exponent == BigInteger.ZERO) return BigInteger.ONE

            var result = BigInteger.ONE
            var b = base.mod(modulus)
            var exp = exponent

            while (exp > BigInteger.ZERO) {
                if (exp.mod(TWO) == BigInteger.ONE) {
                    result = (result * b).mod(modulus)
                }
                exp /= TWO
                b = (b * b).mod(modulus)
            }
            return result
        }

        internal fun computeU(A: BigInteger, B: BigInteger): BigInteger {
            val paddedA = padToGroupSize(bigIntToBytes(A))
            val paddedB = padToGroupSize(bigIntToBytes(B))
            val hash = sha256(paddedA + paddedB)
            return BigInteger.fromByteArray(hash, Sign.POSITIVE)
        }

        internal fun computeX(salt: String, username: String, password: String): BigInteger {
            val innerHash = sha256("$username:$password".encodeToByteArray())
            val saltBytes = hexToBytes(salt)
            val hash = sha256(saltBytes + innerHash)
            return BigInteger.fromByteArray(hash, Sign.POSITIVE)
        }

        internal fun computeM(
            username: String,
            salt: String,
            A: BigInteger,
            B: BigInteger,
            sessionKey: ByteArray,
        ): ByteArray {
            val hashN = sha256(bigIntToBytes(N))
            val hashG = sha256(bigIntToBytes(G))
            val hashNxorG = ByteArray(hashN.size) { i -> (hashN[i].toInt() xor hashG[i].toInt()).toByte() }
            val hashI = sha256(username.encodeToByteArray())
            val saltBytes = hexToBytes(salt)
            val paddedA = padToGroupSize(bigIntToBytes(A))
            val paddedB = padToGroupSize(bigIntToBytes(B))

            return sha256(hashNxorG + hashI + saltBytes + paddedA + paddedB + sessionKey)
        }

        internal fun bigIntToBytes(value: BigInteger): ByteArray {
            val bytes = value.toByteArray()
            return if (bytes.size > 1 && bytes[0] == 0.toByte()) {
                bytes.copyOfRange(1, bytes.size)
            } else {
                bytes
            }
        }

        internal fun padToGroupSize(bytes: ByteArray): ByteArray {
            if (bytes.size >= GROUP_SIZE) return bytes
            return ByteArray(GROUP_SIZE - bytes.size) + bytes
        }

        internal fun hexToBytes(hex: String): ByteArray {
            val cleanHex = if (hex.length % 2 != 0) "0$hex" else hex
            return ByteArray(cleanHex.length / 2) { i ->
                cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        }
    }
}


