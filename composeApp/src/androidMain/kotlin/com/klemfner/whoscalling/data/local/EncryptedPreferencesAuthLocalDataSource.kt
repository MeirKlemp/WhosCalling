package com.klemfner.whoscalling.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.klemfner.whoscalling.domain.model.SavedCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedPreferencesAuthLocalDataSource(
    context: Context,
) : AuthLocalDataSource {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        PREFS_FILE,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _savedCredentials = MutableStateFlow(loadFromPrefs())
    override val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    override fun saveCredentials(credentials: SavedCredentials) {
        prefs.edit()
            .putString(KEY_USERNAME, credentials.username)
            .putString(KEY_PASSWORD, credentials.password)
            .putLong(KEY_LOGIN_TIME, credentials.loginTime)
            .putString(KEY_SESSION_KEY, credentials.sessionKey)
            .apply()
        _savedCredentials.value = credentials
    }

    override fun clearCredentials() {
        prefs.edit().clear().apply()
        _savedCredentials.value = null
    }

    private fun loadFromPrefs(): SavedCredentials? {
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, -1L)
        val sessionKey = prefs.getString(KEY_SESSION_KEY, null) ?: return null
        if (loginTime == -1L) return null
        return SavedCredentials(username, password, loginTime, sessionKey)
    }

    companion object {
        private const val PREFS_FILE = "auth_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_SESSION_KEY = "session_key"
    }
}
