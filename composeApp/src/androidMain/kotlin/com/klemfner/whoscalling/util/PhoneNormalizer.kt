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

actual fun normalizePhoneNumberWithRegion(phoneNumber: String, defaultRegion: String?): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val parsed = phoneUtil.parse(phoneNumber, defaultRegion)
    if (!phoneUtil.isValidNumber(parsed)) {
        throw IllegalArgumentException("Invalid phone number: $phoneNumber")
    }
    return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
}

actual fun formatPhoneForDisplay(phoneNumber: String, defaultCountryIso: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val number = phoneUtil.parse(phoneNumber, null)
        val countryCode = number.countryCode
        val defaultCountryCode = phoneUtil.getCountryCodeForRegion(defaultCountryIso)
        val national = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        if (defaultCountryIso.isNotEmpty() && countryCode == defaultCountryCode) {
            national
        } else {
            "+$countryCode | $national"
        }
    } catch (e: Exception) {
        Logger.w("PhoneNormalizer", "Failed to format phone for display: $phoneNumber", e)
        phoneNumber
    }
}

actual fun getCountryIsoFromPhoneNumber(phoneNumber: String): String? {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val number = phoneUtil.parse(phoneNumber, null)
        phoneUtil.getRegionCodeForNumber(number)
    } catch (_: Exception) {
        null
    }
}
