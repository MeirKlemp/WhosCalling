package com.klemfner.whoscalling.util

data class FormattedPhone(
    val internationalPrefix: String?,
    val nationalNumber: String,
) {
    override fun toString(): String =
        if (internationalPrefix != null) "$internationalPrefix | $nationalNumber"
        else nationalNumber
}
