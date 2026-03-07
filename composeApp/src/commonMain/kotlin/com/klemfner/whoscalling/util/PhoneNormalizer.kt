package com.klemfner.whoscalling.util

expect fun normalizePhoneNumber(phoneNumber: String, defaultRegion: String? = null): String

expect fun formatPhoneForDisplay(phoneNumber: String, defaultCountryIso: String): FormattedPhone

expect fun getCountryIsoFromPhoneNumber(phoneNumber: String): String?
