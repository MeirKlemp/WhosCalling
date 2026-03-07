package com.klemfner.whoscalling.util

actual fun defaultCountryIso(): String =
    java.util.Locale.getDefault().country.ifEmpty { "US" }
