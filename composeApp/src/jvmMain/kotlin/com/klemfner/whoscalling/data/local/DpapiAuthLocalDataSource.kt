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

        internal fun serialize(credentials: SavedCredentials): String {
            val encoder = java.util.Base64.getEncoder()
            return listOf(
                encoder.encodeToString(credentials.username.toByteArray(Charsets.UTF_8)),
                encoder.encodeToString(credentials.password.toByteArray(Charsets.UTF_8)),
                credentials.loginTime.toString(),
                encoder.encodeToString(credentials.sessionKey.toByteArray(Charsets.UTF_8)),
            ).joinToString("\n")
        }

        internal fun deserialize(data: String): SavedCredentials? {
            val parts = data.split("\n")
            if (parts.size != 4) return null
            return try {
                val decoder = java.util.Base64.getDecoder()
                SavedCredentials(
                    username = String(decoder.decode(parts[0]), Charsets.UTF_8),
                    password = String(decoder.decode(parts[1]), Charsets.UTF_8),
                    loginTime = parts[2].toLong(),
                    sessionKey = String(decoder.decode(parts[3]), Charsets.UTF_8),
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
