package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.SavedCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class SecretToolAuthLocalDataSource : AuthLocalDataSource {

    private val _savedCredentials = MutableStateFlow(loadFromKeyring())
    override val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    override fun saveCredentials(credentials: SavedCredentials) {
        val data = serialize(credentials)
        val process = ProcessBuilder(
            "secret-tool", "store",
            "--label=$APP_LABEL",
            ATTR_APPLICATION, APP_NAME,
        ).redirectErrorStream(true).start()
        process.outputStream.use { it.write(data.toByteArray(Charsets.UTF_8)) }
        process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        _savedCredentials.value = credentials
    }

    override fun clearCredentials() {
        runCommand("secret-tool", "clear", ATTR_APPLICATION, APP_NAME)
        _savedCredentials.value = null
    }

    private fun loadFromKeyring(): SavedCredentials? {
        val data = runCommand("secret-tool", "lookup", ATTR_APPLICATION, APP_NAME)
            ?: return null
        return deserialize(data)
    }

    private fun runCommand(vararg command: String): String? {
        return try {
            val process = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (process.exitValue() == 0 && output.isNotEmpty()) output else null
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val APP_NAME = "WhosCalling"
        private const val APP_LABEL = "WhosCalling Credentials"
        private const val ATTR_APPLICATION = "application"
        private const val TIMEOUT_SECONDS = 5L
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
