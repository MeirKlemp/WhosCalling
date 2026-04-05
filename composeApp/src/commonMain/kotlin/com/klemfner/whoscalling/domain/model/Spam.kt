package com.klemfner.whoscalling.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Spam(
    val phoneNumber: String,
    val detectedAsSpam: Boolean = false,
    val reportedAs: SpamReport? = null,
    val detectionTimestamp: Long? = null,
    val reportingTimestamp: Long? = null,
) {
    val isSpam: Boolean
        get() = (detectedAsSpam || reportedAs == SpamReport.SPAM) && reportedAs != SpamReport.SAFE
}

@Serializable
enum class SpamReport {
    SPAM,
    SAFE,
}
