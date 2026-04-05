package com.klemfner.whoscalling.ui.ringing_banner

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact

data class RingingCallUiState(
    val ringingCall: CallLog? = null,
    val contact: Contact? = null,
    val defaultCountryIso: String = "",
    val isDismissed: Boolean = false,
    val isSpam: Boolean = false,
) {
    val showBanner = ringingCall != null && !isDismissed
}