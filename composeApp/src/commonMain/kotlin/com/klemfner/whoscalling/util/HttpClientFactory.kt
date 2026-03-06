package com.klemfner.whoscalling.util

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
