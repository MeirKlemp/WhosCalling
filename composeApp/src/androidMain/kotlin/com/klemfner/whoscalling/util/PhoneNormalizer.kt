package com.klemfner.whoscalling.util

import com.google.i18n.phonenumbers.PhoneNumberUtil

actual fun normalizePhoneNumber(phoneNumber: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val parsed = phoneUtil.parse(phoneNumber, null)
    if (!phoneUtil.isValidNumber(parsed)) {
        throw IllegalArgumentException("Invalid phone number: $phoneNumber")
    }
    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
}
