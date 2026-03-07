package com.klemfner.whoscalling.util

expect fun normalizePhoneNumber(phoneNumber: String): String

expect fun normalizePhoneNumberWithRegion(phoneNumber: String, defaultRegion: String?): String

expect fun formatPhoneForDisplay(phoneNumber: String, defaultCountryIso: String): String

expect fun getCountryIsoFromPhoneNumber(phoneNumber: String): String?
