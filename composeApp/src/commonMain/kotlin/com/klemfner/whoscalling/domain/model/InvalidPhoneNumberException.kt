package com.klemfner.whoscalling.domain.model

class InvalidPhoneNumberException(phoneNumber: String) :
    Exception("Invalid phone number format: $phoneNumber")
