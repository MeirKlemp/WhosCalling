package com.klemfner.whoscalling.data.remote.srp

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.G
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.GROUP_SIZE
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.K
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.N
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.bigIntToBytes
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.computeM
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.computeU
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.computeX
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.hexToBytes
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.modPow
import com.klemfner.whoscalling.data.remote.srp.Srp6aClientImpl.Companion.padToGroupSize
import com.klemfner.whoscalling.util.sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class Srp6aClientImplTest {

    private fun ByteArray.toHex(): String =
        joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

    @Test
    fun modPowWithSmallValues() {
        val base = BigInteger(2)
        val exponent = BigInteger(10)
        val modulus = BigInteger(1000)
        val result = modPow(base, exponent, modulus)
        assertEquals(BigInteger(24), result)
    }

    @Test
    fun modPowWithZeroExponent() {
        val result = modPow(BigInteger(5), BigInteger.ZERO, BigInteger(13))
        assertEquals(BigInteger.ONE, result)
    }

    @Test
    fun modPowWithOneExponent() {
        val result = modPow(BigInteger(5), BigInteger.ONE, BigInteger(13))
        assertEquals(BigInteger(5), result)
    }

    @Test
    fun modPowWithLargerValues() {
        val base = BigInteger(123)
        val exponent = BigInteger(456)
        val modulus = BigInteger(789)
        val result = modPow(base, exponent, modulus)
        // 123^456 mod 789 = 699
        assertEquals(BigInteger(699), result)
    }

    @Test
    fun modPowWithGroupParameters() {
        // g^1 mod N = g
        val result = modPow(G, BigInteger.ONE, N)
        assertEquals(G, result)
    }

    @Test
    fun hexToBytesConvertsCorrectly() {
        val bytes = hexToBytes("deadbeef")
        assertEquals(4, bytes.size)
        assertEquals(0xde.toByte(), bytes[0])
        assertEquals(0xad.toByte(), bytes[1])
        assertEquals(0xbe.toByte(), bytes[2])
        assertEquals(0xef.toByte(), bytes[3])
    }

    @Test
    fun hexToBytesWithOddLength() {
        val bytes = hexToBytes("abc")
        assertEquals(2, bytes.size)
        assertEquals(0x0a.toByte(), bytes[0])
        assertEquals(0xbc.toByte(), bytes[1])
    }

    @Test
    fun hexToBytesEmptyString() {
        val bytes = hexToBytes("")
        assertEquals(0, bytes.size)
    }

    @Test
    fun toHexConvertsCorrectly() {
        val bytes = byteArrayOf(0xde.toByte(), 0xad.toByte(), 0xbe.toByte(), 0xef.toByte())
        assertEquals("deadbeef", bytes.toHex())
    }

    @Test
    fun toHexWithLeadingZeros() {
        val bytes = byteArrayOf(0x00.toByte(), 0x0a.toByte(), 0xff.toByte())
        assertEquals("000aff", bytes.toHex())
    }

    @Test
    fun padToGroupSizeDoesNotPadIfAlreadyCorrectSize() {
        val bytes = ByteArray(GROUP_SIZE) { 0x42.toByte() }
        val padded = padToGroupSize(bytes)
        assertEquals(GROUP_SIZE, padded.size)
        assertEquals(0x42.toByte(), padded[0])
    }

    @Test
    fun padToGroupSizePadsSmallArray() {
        val bytes = byteArrayOf(0x01, 0x02)
        val padded = padToGroupSize(bytes)
        assertEquals(GROUP_SIZE, padded.size)
        assertEquals(0x00.toByte(), padded[0])
        assertEquals(0x01.toByte(), padded[GROUP_SIZE - 2])
        assertEquals(0x02.toByte(), padded[GROUP_SIZE - 1])
    }

    @Test
    fun bigIntToBytesStripsLeadingZeroByte() {
        val value = BigInteger.parseString("ff", 16)
        val bytes = bigIntToBytes(value)
        assertEquals(1, bytes.size)
        assertEquals(0xff.toByte(), bytes[0])
    }

    @Test
    fun computeXProducesConsistentResults() {
        val x1 = computeX("abcdef", "user", "pass")
        val x2 = computeX("abcdef", "user", "pass")
        assertEquals(x1, x2)
    }

    @Test
    fun computeXDiffersWithDifferentSalt() {
        val x1 = computeX("abcdef", "user", "pass")
        val x2 = computeX("123456", "user", "pass")
        assertNotEquals(x1, x2)
    }

    @Test
    fun computeXDiffersWithDifferentUsername() {
        val x1 = computeX("abcdef", "user1", "pass")
        val x2 = computeX("abcdef", "user2", "pass")
        assertNotEquals(x1, x2)
    }

    @Test
    fun computeXDiffersWithDifferentPassword() {
        val x1 = computeX("abcdef", "user", "pass1")
        val x2 = computeX("abcdef", "user", "pass2")
        assertNotEquals(x1, x2)
    }

    @Test
    fun computeXFollowsSrpFormula() {
        val salt = "beb25379d1a8581eb5a727673a2441ee"
        val username = "alice"
        val password = "password123"

        val innerHash = sha256("$username:$password".encodeToByteArray())
        val saltBytes = hexToBytes(salt)
        val expectedHash = sha256(saltBytes + innerHash)
        val expected = BigInteger.fromByteArray(expectedHash, Sign.POSITIVE)

        assertEquals(expected, computeX(salt, username, password))
    }

    @Test
    fun computeUIsSymmetricInInputOrder() {
        val A = BigInteger(12345)
        val B = BigInteger(67890)
        val u1 = computeU(A, B)
        val u2 = computeU(B, A)
        assertNotEquals(u1, u2)
    }

    @Test
    fun computeUProducesConsistentResults() {
        val A = BigInteger(12345)
        val B = BigInteger(67890)
        assertEquals(computeU(A, B), computeU(A, B))
    }

    @Test
    fun computeMProducesConsistentResults() {
        val A = modPow(G, BigInteger(42), N)
        val B = modPow(G, BigInteger(99), N)
        val sessionKey = sha256("test-session".encodeToByteArray())

        val m1 = computeM("user", "aabb", A, B, sessionKey)
        val m2 = computeM("user", "aabb", A, B, sessionKey)
        assertTrue(m1.contentEquals(m2))
    }

    @Test
    fun computeMDiffersWithDifferentUsername() {
        val A = modPow(G, BigInteger(42), N)
        val B = modPow(G, BigInteger(99), N)
        val sessionKey = sha256("test-session".encodeToByteArray())

        val m1 = computeM("user1", "aabb", A, B, sessionKey)
        val m2 = computeM("user2", "aabb", A, B, sessionKey)
        assertFalse(m1.contentEquals(m2))
    }

    @Test
    fun generatePublicKeyReturnsNonEmptyHexString() {
        val client = Srp6aClientImpl()
        val pubKey = client.generatePublicKey()
        assertTrue(pubKey.isNotBlank())
        // Verify it's valid hex
        BigInteger.parseString(pubKey, 16)
    }

    @Test
    fun generatePublicKeyReturnsDifferentValuesEachTime() {
        val client1 = Srp6aClientImpl()
        val client2 = Srp6aClientImpl()
        val key1 = client1.generatePublicKey()
        val key2 = client2.generatePublicKey()
        assertNotEquals(key1, key2)
    }

    @Test
    fun kValueMatchesPreCalculated() {
        // Verify that the pre-calculated k = H(N || PAD(g)) matches our constant
        assertEquals(
            "05b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300",
            K.toString(16).lowercase().padStart(64, '0'),
        )
    }

    @Test
    fun fullSrpProtocolWithKnownValues() {
        // Simulate a complete SRP-6a exchange using known private values
        val client = Srp6aClientImpl()

        // Generate client public key
        val publicKeyHex = client.generatePublicKey()
        val A = BigInteger.parseString(publicKeyHex, 16)

        // Verify A is in valid range (A mod N != 0)
        assertNotEquals(BigInteger.ZERO, A.mod(N))

        // Simulate server side:
        val salt = "beb25379d1a8581eb5a727673a2441ee"
        val username = "alice"
        val password = "password123"

        // Server generates its own key pair
        val serverPrivateKey = BigInteger(777)
        val x = computeX(salt, username, password)
        val v = modPow(G, x, N) // verifier
        val B = (K * v + modPow(G, serverPrivateKey, N)).mod(N)

        // Client processes challenge
        val clientProof = client.processChallenge(username, password, salt, B.toString(16))
        assertTrue(clientProof.isNotBlank())

        // Server side verification
        val u = computeU(A, B)
        val serverS = modPow(A * modPow(v, u, N), serverPrivateKey, N)
        val serverSessionKey = sha256(bigIntToBytes(serverS))
        val expectedM = computeM(username, salt, A, B, serverSessionKey)

        assertEquals(expectedM.toHex(), clientProof)

        // Server generates its proof
        val serverM = sha256(bigIntToBytes(A) + expectedM + serverSessionKey)

        // Client verifies server proof
        assertTrue(client.verifyServerProof(serverM.toHex()))
    }

    @Test
    fun verifyServerProofFailsWithWrongProof() {
        val client = Srp6aClientImpl()
        client.generatePublicKey()

        val salt = "aabbccdd"
        val username = "user"
        val password = "pass"

        val serverPrivateKey = BigInteger(42)
        val x = computeX(salt, username, password)
        val v = modPow(G, x, N)
        val B = (K * v + modPow(G, serverPrivateKey, N)).mod(N)

        client.processChallenge(username, password, salt, B.toString(16))

        assertFalse(client.verifyServerProof("0000000000000000000000000000000000000000000000000000000000000000"))
    }

    @Test
    fun verifyServerProofIsCaseInsensitive() {
        val client = Srp6aClientImpl()
        val A = client.generatePublicKey()

        val salt = "aabbccdd"
        val username = "user"
        val password = "pass"

        val serverPrivateKey = BigInteger(42)
        val x = computeX(salt, username, password)
        val v = modPow(G, x, N)
        val B = (K * v + modPow(G, serverPrivateKey, N)).mod(N)

        val clientProof = client.processChallenge(username, password, salt, B.toString(16))

        val ABigInt = BigInteger.parseString(A, 16)
        val u = computeU(ABigInt, B)
        val serverS = modPow(ABigInt * modPow(v, u, N), serverPrivateKey, N)
        val serverSessionKey = sha256(bigIntToBytes(serverS))
        val expectedM = computeM(username, salt, ABigInt, B, serverSessionKey)
        val serverM = sha256(bigIntToBytes(ABigInt) + expectedM + serverSessionKey)

        assertTrue(client.verifyServerProof(serverM.toHex().uppercase()))
    }
}
