package com.adrianosilva.simply_works.domain.usecase

import android.content.Context
import android.net.wifi.WifiManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class NetworkScanner(private val context: Context) {

    private fun getLocalIpAddress(): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddressInt = wifiManager.connectionInfo.ipAddress
        if (ipAddressInt == 0) return null

        return String.format(
            "%d.%d.%d.%d",
            ipAddressInt and 0xff,
            ipAddressInt shr 8 and 0xff,
            ipAddressInt shr 16 and 0xff,
            ipAddressInt shr 24 and 0xff
        )
    }

    suspend fun scanNetwork(): String? = coroutineScope {
        val localIp = getLocalIpAddress()
        if (localIp == null) {
            Timber.e("Could not determine local IP address")
            return@coroutineScope null
        }
        
        Timber.d("Local IP: $localIp")
        val subnet = localIp.substringBeforeLast(".")

        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 1500
                connectTimeoutMillis = 500
            }
        }

        val deferreds = (1..254).map { i ->
            async(Dispatchers.IO) {
                val ipToTest = "$subnet.$i"
                try {
                    val response: HttpResponse = client.get("http://$ipToTest/http-read.json?encrypted=1")
                    if (response.status.value in 200..299) {
                        val body = response.bodyAsText().trim()
                        if (body.isNotEmpty() && body.matches(Regex("^[0-9A-Fa-f]+$"))) {
                            Timber.d("Found washing machine at $ipToTest")
                            return@async ipToTest
                        }
                    }
                } catch (e: Exception) {
                    // Ignore timeouts or connection refused
                }
                return@async null
            }
        }

        val result = deferreds.awaitAll().firstOrNull { it != null }
        client.close()
        return@coroutineScope result
    }
}
