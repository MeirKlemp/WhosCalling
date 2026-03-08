package com.klemfner.whoscalling.util

fun maskPhoneNumber(phoneNumber: String): String {
    val digitPositions = phoneNumber.indices.filter { phoneNumber[it].isDigit() }
    if (digitPositions.size < 6) return phoneNumber

    val toMask = digitPositions.subList(digitPositions.size - 6, digitPositions.size - 2).toSet()

    return buildString(phoneNumber.length) {
        phoneNumber.forEachIndexed { index, char ->
            append(if (index in toMask) '*' else char)
        }
    }
}
