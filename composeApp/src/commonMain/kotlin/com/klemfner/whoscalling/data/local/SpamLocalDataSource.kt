package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.Spam
import kotlinx.coroutines.flow.Flow

interface SpamLocalDataSource {
    val spams: Flow<List<Spam>>
    suspend fun getSpam(phoneNumber: String): Spam?
    suspend fun saveSpam(spam: Spam)
    suspend fun deleteSpam(phoneNumber: String)
    suspend fun deleteAllSpams()
}
