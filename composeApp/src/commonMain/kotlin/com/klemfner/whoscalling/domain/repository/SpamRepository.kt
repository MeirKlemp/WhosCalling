package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.Spam
import kotlinx.coroutines.flow.Flow

interface SpamRepository {
    val spams: Flow<List<Spam>>
    suspend fun getSpam(phoneNumber: String): Spam?
    suspend fun reportAsSpam(phoneNumber: String)
    suspend fun reportAsSafe(phoneNumber: String)
    suspend fun addDetectedSpam(phoneNumber: String)
    suspend fun deleteSpam(phoneNumber: String)
    suspend fun addSpams(spams: List<Spam>): Int
}
