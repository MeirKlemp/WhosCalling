package com.klemfner.whoscalling.util

actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    java.security.SecureRandom().nextBytes(bytes)
    return bytes
}
