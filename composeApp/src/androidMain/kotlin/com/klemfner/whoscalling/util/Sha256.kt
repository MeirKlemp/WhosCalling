package com.klemfner.whoscalling.util

actual fun sha256(data: ByteArray): ByteArray =
    java.security.MessageDigest.getInstance("SHA-256").digest(data)
