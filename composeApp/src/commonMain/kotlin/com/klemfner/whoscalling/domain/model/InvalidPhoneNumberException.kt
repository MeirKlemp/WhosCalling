package com.klemfner.whoscalling.domain.model

import com.klemfner.whoscalling.util.maskPhoneNumber

class InvalidPhoneNumberException(phoneNumber: String) :
    Exception("Invalid phone number format: ${maskPhoneNumber(phoneNumber)}")
