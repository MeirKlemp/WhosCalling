package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.SavedCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthLocalDataSource : AuthLocalDataSource {
    private val _savedCredentials = MutableStateFlow<SavedCredentials?>(null)
    override val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    override fun saveCredentials(credentials: SavedCredentials) {
        _savedCredentials.value = credentials
    }

    override fun clearCredentials() {
        _savedCredentials.value = null
    }
}
