package com.klemfner.whoscalling.ui.contacts

sealed interface ContactsError {
    // Form errors
    sealed interface FormError : ContactsError
    data object InvalidPhoneNumber : FormError
    data object GenericFormError : FormError

    // Delete errors
    data object GenericDeleteError : ContactsError
}
