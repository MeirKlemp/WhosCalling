package com.klemfner.whoscalling.domain.model

import com.klemfner.whoscalling.util.maskPhoneNumber

class InvalidPhoneNumberException(phoneNumber: String, cause: Throwable? = null) :
    Exception("Invalid phone number format: ${maskPhoneNumber(phoneNumber)}", cause)
