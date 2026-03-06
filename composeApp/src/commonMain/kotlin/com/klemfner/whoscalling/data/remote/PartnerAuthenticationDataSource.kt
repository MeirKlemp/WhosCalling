package com.klemfner.whoscalling.data.remote

import com.fleeksoft.ksoup.Ksoup
import com.klemfner.whoscalling.data.remote.srp.Srp6aClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.setCookie

/**
 * Remote authentication data source that authenticates against the partner router
 * using the SRP-6a protocol.
 *
 * Login flow:
 * 1. GET /login.lp  — obtain the session cookie (sessionID) and CSRF token.
 * 2. POST /authenticate with username and SRP public key A.
 * 3. Receive the server's salt (s) and public key (B).
 * 4. Compute the client proof M1 via [srp6aClient].
 * 5. POST /authenticate with M1.
 * 6. Receive and validate the server proof M2 via [srp6aClient].
 * 7. Return the sessionID from step 1 as the authentication token.
 *
 * TODO: Make the router IP configurable instead of hard-coding it.
 *       Tracked in: https://github.com/MeirKlemp/WhosCalling/issues/1
 */
class PartnerAuthenticationDataSource(
    private val srp6aClient: Srp6aClient,
    private val httpClient: HttpClient,
) : AuthRemoteDataSource {

    override suspend fun login(username: String, password: String): String {
        if (username.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Username and password must not be blank")
        }

        val baseUrl = "http://192.168.60.1"

        // Step 1: GET /login.lp
        val loginResponse = httpClient.get("$baseUrl/login.lp")

        // Step 2: Extract the sessionID from the Set-Cookie header
        val sessionId = loginResponse.setCookie()
            .find { it.name == "sessionID" }
            ?.value
            ?: error("sessionID cookie not found in response from /login.lp")

        // Step 3: Extract the CSRF token from the HTML meta tag
        val html = loginResponse.bodyAsText()
        val document = Ksoup.parse(html)
        val csrfToken = document.select("meta[name=CSRFtoken]").attr("content")
            .takeIf { it.isNotBlank() }
            ?: error("CSRFtoken meta tag not found in /login.lp response")

        // Step 4: Generate the SRP client public key A
        val publicKeyA = srp6aClient.generatePublicKey()

        // Step 5: POST username and public key to /authenticate
        val authResponse1 = httpClient.post("$baseUrl/authenticate") {
            header(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
            setBody(buildFormBody("CSRFtoken" to csrfToken, "I" to username, "A" to publicKeyA))
        }
        val authJson1 = authResponse1.bodyAsText()

        // Step 5 (cont): Extract salt and server public key from JSON
        val salt = parseJsonField(authJson1, "s")
        val serverPublicKeyB = parseJsonField(authJson1, "B")

        // Step 6: Compute the client proof M1 using the SRP client
        val clientProofM1 = srp6aClient.calculateClientProof(
            username = username,
            password = password,
            salt = salt,
            serverPublicKey = serverPublicKeyB,
        )

        // Step 7: POST the client proof M1 to /authenticate
        val authResponse2 = httpClient.post("$baseUrl/authenticate") {
            header(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
            setBody(buildFormBody("CSRFtoken" to csrfToken, "M" to clientProofM1))
        }
        val authJson2 = authResponse2.bodyAsText()

        // Step 8: Extract server proof M2 from JSON
        val serverProofM2 = parseJsonField(authJson2, "M")

        // Step 9: Validate the server proof
        if (!srp6aClient.validateServerProof(serverProofM2)) {
            error("Server authentication proof validation failed — credentials may be incorrect")
        }

        // Step 10: Return the session token obtained in step 2
        return sessionId
    }

    /**
     * Parses a simple string field from a JSON object.
     * Expects the format: `"field": "value"` (double-quoted string values only).
     */
    private fun parseJsonField(json: String, field: String): String {
        val pattern = Regex(""""$field"\s*:\s*"([^"]+)"""")
        return pattern.find(json)?.groupValues?.get(1)
            ?: error("Field \"$field\" not found in JSON response: $json")
    }

    /**
     * Builds an `application/x-www-form-urlencoded` body string.
     * Values are percent-encoded to be safe in form submissions.
     */
    private fun buildFormBody(vararg pairs: Pair<String, String>): String =
        pairs.joinToString("&") { (key, value) ->
            "${percentEncode(key)}=${percentEncode(value)}"
        }

    /**
     * Minimal percent-encoding for form values.
     * Encodes all characters that are not unreserved per RFC 3986.
     */
    private fun percentEncode(value: String): String {
        val sb = StringBuilder()
        for (ch in value) {
            if (ch.isLetterOrDigit() || ch == '-' || ch == '_' || ch == '.' || ch == '~') {
                sb.append(ch)
            } else {
                ch.toString().encodeToByteArray().forEach { byte ->
                    sb.append('%')
                    sb.append("0123456789ABCDEF"[(byte.toInt() and 0xFF) shr 4])
                    sb.append("0123456789ABCDEF"[byte.toInt() and 0x0F])
                }
            }
        }
        return sb.toString()
    }
}
