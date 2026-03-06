package com.klemfner.whoscalling.util

private val secureRandom = java.security.SecureRandom()

actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    secureRandom.nextBytes(bytes)
    return bytes
}
