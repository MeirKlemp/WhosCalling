package com.klemfner.whoscalling.data.remote

import com.fleeksoft.ksoup.Ksoup
import com.klemfner.whoscalling.data.remote.srp.Srp6aClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PartnerAuthenticationDataSource(
    private val srpClient: Srp6aClient,
    private val httpClient: HttpClient,
    private val routerIp: () -> String,
) : AuthRemoteDataSource {

    override suspend fun login(username: String, password: String): String = withContext(Dispatchers.Default) {
        if (username.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Username and password must not be blank")
        }

        val baseUrl = "http://${routerIp()}"
        val loginResponse = httpClient.get("$baseUrl/login.lp")

        val initialSessionId = extractSessionId(loginResponse.headers["Set-Cookie"])
            ?: throw IllegalStateException("No sessionID cookie found")

        val loginHtml = loginResponse.bodyAsText()
        val document = Ksoup.parse(loginHtml)
        val csrfToken = document.select("meta[name=CSRFtoken]").attr("content")
        if (csrfToken.isBlank()) {
            throw IllegalStateException("No CSRF token found")
        }

        val publicKey = srpClient.generatePublicKey()

        val challengeResponse = httpClient.post("$baseUrl/authenticate") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("Cookie", "sessionID=$initialSessionId")
            setBody("CSRFtoken=$csrfToken&I=$username&A=$publicKey")
        }

        val challengeBody = challengeResponse.bodyAsText()
        val salt = extractJsonStringValue(challengeBody, "s")
            ?: throw IllegalStateException("No salt in server response")
        val serverPublicKey = extractJsonStringValue(challengeBody, "B")
            ?: throw IllegalStateException("No server public key in response")

        val clientProof = srpClient.processChallenge(username, password, salt, serverPublicKey)

        val proofResponse = httpClient.post("$baseUrl/authenticate") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("Cookie", "sessionID=$initialSessionId")
            setBody("CSRFtoken=$csrfToken&M=$clientProof")
        }

        val proofBody = proofResponse.bodyAsText()
        val serverProof = extractJsonStringValue(proofBody, "M")
            ?: throw IllegalStateException("No server proof in response")

        if (!srpClient.verifyServerProof(serverProof)) {
            throw IllegalStateException("Server proof verification failed")
        }

        val sessionId = extractSessionId(proofResponse.headers["Set-Cookie"])
            ?: initialSessionId

        sessionId
    }

    private fun extractJsonStringValue(json: String, key: String): String? {
        val regex = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"([^\"]+)\"")
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun extractSessionId(setCookie: String?): String? {
        return setCookie
            ?.split(";")
            ?.firstOrNull { it.trim().startsWith("sessionID=") }
            ?.substringAfter("sessionID=")
    }
}
