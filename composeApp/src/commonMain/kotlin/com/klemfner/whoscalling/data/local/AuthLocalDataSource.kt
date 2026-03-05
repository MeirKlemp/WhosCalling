package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.SavedCredentials
import kotlinx.coroutines.flow.StateFlow

interface AuthLocalDataSource {
    val savedCredentials: StateFlow<SavedCredentials?>
    fun saveCredentials(credentials: SavedCredentials)
    fun clearCredentials()
}
