package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.SavedCredentials
import com.sun.jna.platform.win32.Crypt32Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DpapiAuthLocalDataSource(
    appDataDir: File = File(
        System.getenv("APPDATA") ?: System.getProperty("user.home"),
        "WhosCalling",
    ),
) : AuthLocalDataSource {

    private val credFile = File(appDataDir, CRED_FILE_NAME)

    private val _savedCredentials = MutableStateFlow(loadFromFile())
    override val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    override fun saveCredentials(credentials: SavedCredentials) {
        credFile.parentFile?.mkdirs()
        val plaintext = serialize(credentials).toByteArray(Charsets.UTF_8)
        val encrypted = Crypt32Util.cryptProtectData(plaintext)
        credFile.writeBytes(encrypted)
        _savedCredentials.value = credentials
    }

    override fun clearCredentials() {
        credFile.delete()
        _savedCredentials.value = null
    }

    private fun loadFromFile(): SavedCredentials? {
        if (!credFile.exists()) return null
        return try {
            val encrypted = credFile.readBytes()
            val decrypted = Crypt32Util.cryptUnprotectData(encrypted)
            deserialize(String(decrypted, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val CRED_FILE_NAME = "credentials.enc"
        private const val FIELD_SEPARATOR = "\u0000"

        internal fun serialize(credentials: SavedCredentials): String {
            return listOf(
                credentials.username,
                credentials.password,
                credentials.loginTime.toString(),
                credentials.sessionKey,
            ).joinToString(FIELD_SEPARATOR)
        }

        internal fun deserialize(data: String): SavedCredentials? {
            val parts = data.split(FIELD_SEPARATOR)
            if (parts.size != 4) return null
            return try {
                SavedCredentials(
                    username = parts[0],
                    password = parts[1],
                    loginTime = parts[2].toLong(),
                    sessionKey = parts[3],
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
