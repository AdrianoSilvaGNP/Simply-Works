package com.adrianosilva.simply_works.data.remote

import com.adrianosilva.simply_works.Utils.bytesToHex
import com.adrianosilva.simply_works.Utils.decrypt
import com.adrianosilva.simply_works.Utils.deriveKey
import com.adrianosilva.simply_works.Utils.encrypt
import com.adrianosilva.simply_works.Utils.hexToBytes
import com.adrianosilva.simply_works.data.dto.MachineStatusResponse
import com.adrianosilva.simply_works.data.dto.MachineUsageStatsResponse
import com.adrianosilva.simply_works.data.dto.StatusCounters
import com.adrianosilva.simply_works.data.dto.WashProgramRequest
import com.adrianosilva.simply_works.domain.ErrorReason
import com.adrianosilva.simply_works.domain.Result
import com.adrianosilva.simply_works.domain.models.MachineState
import com.adrianosilva.simply_works.domain.models.WashProgram
import com.adrianosilva.simply_works.domain.models.WashProgramPhase
import com.adrianosilva.simply_works.domain.models.WashingMachineStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Service class for interacting with the Candy washing machine API.
 *
 * @property baseUrl The base URL of the Candy washing machine API.
 * @property xorKey The XOR key used for encrypting and decrypting data.
 */
class CandyApiService(private val baseUrl: String, private var xorKey: ByteArray) {

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 3000
                connectTimeoutMillis = 3000
            }
        }

    fun deriveKey(onKeyDerived: ((ByteArray) -> Unit)? = null) {
        if (xorKey.isEmpty()) {
            CoroutineScope(Dispatchers.Default).launch {
                when (val result = readData()) {
                    is Result.Success -> {
                        val encrypted = result.data
                        Timber.d("encrypted data: $encrypted")

                        val encryptedBytes = hexToBytes(encrypted)
                        val derivedKey = deriveKey(encryptedBytes)
                        xorKey = derivedKey
                        Timber.d("Derived XOR key: ${xorKey.toString(Charsets.UTF_8)}")
                        onKeyDerived?.invoke(derivedKey)
                    }

                    is Result.Error -> {
                        Timber.e("Failed to derive key: ${result.reason}")
                    }
                }
            }
        }
    }

    suspend fun readData(): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("$baseUrl/http-read.json?encrypted=1") {
                    accept(ContentType.Text.Html)
                }.body<String>()

                Result.Success(response)
            } catch (e: Throwable) {
                Timber.e("Error reading data from washing machine: ${e.localizedMessage}")
                Result.Error(e.toErrorReason())
            }
        }

    suspend fun writeData(data: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("$baseUrl/http-write.json?encrypted=1&data=$data") {
                    accept(ContentType.Text.Html)
                }.body<String>()

                Result.Success(response)
            } catch (e: Throwable) {
                Timber.e("Error writing data to washing machine: ${e.localizedMessage}")
                Result.Error(e.toErrorReason())
            }
        }

    suspend fun sendWashRequest(request: WashProgramRequest): Result<Unit> {
        val requestArray = request.toQueryString().toByteArray()

        Timber.d("Wash program request: ${requestArray.decodeToString()}")
        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Wash program command (encrypted): $stringMessage")

        return when (val result = writeData(stringMessage)) {
            is Result.Success -> {
                try {
                    val encryptedBytes = hexToBytes(result.data)
                    val decrypted = decrypt(xorKey, encryptedBytes)
                    Timber.d("Wash program command response (decrypted): ${decrypted.decodeToString()}")
                    Result.Success(Unit)
                } catch (e: Exception) {
                    Result.Error(ErrorReason.Unknown(e))
                }
            }

            is Result.Error -> Result.Error(result.reason)
        }
    }

    suspend fun getStatus(): Result<WashingMachineStatus> = withContext(Dispatchers.Default) {
        when (val result = readData()) {
            is Result.Success -> {
                try {
                    val encrypted = result.data
                    Timber.d("encrypted data: $encrypted")

                    val encryptedBytes = hexToBytes(encrypted)
                    val decrypted = decrypt(xorKey, encryptedBytes)
                    val jsonString = decrypted.decodeToString()
                    Timber.d("Decrypted data: $jsonString")

                    if (jsonString.isEmpty()) {
                        return@withContext Result.Error(ErrorReason.NoData)
                    }

                    val response = json.decodeFromString<MachineStatusResponse>(jsonString)

                    val remainingTimeMinutes = response.statusLavatrice.remainingTime.toInt() / 60
                    val hours = remainingTimeMinutes / 60
                    val minutes = remainingTimeMinutes % 60

                    val formattedTime = when {
                        hours > 0 -> "${hours}h ${minutes}m"
                        else -> "$minutes min"
                    }

                    Result.Success(
                        WashingMachineStatus(
                            machineState = MachineState.fromCode(response.statusLavatrice.machineMode.toInt()),
                            programState = WashProgramPhase.fromCode(response.statusLavatrice.programPhase.toInt()),
                            program = WashProgram.allPrograms.first {
                                it.number == response.statusLavatrice.programNumber.toInt()
                            },
                            temp = response.statusLavatrice.temp.toInt(),
                            spinSpeed = response.statusLavatrice.spinSpeed.toInt() * 100,
                            remainingTime = formattedTime,
                            delayMinutes = response.statusLavatrice.delayValue.toInt()
                        )
                    )
                } catch (e: Throwable) {
                    Result.Error(ErrorReason.Unknown(e))
                }
            }

            is Result.Error -> Result.Error(result.reason)
        }
    }

    suspend fun callResetWashCycle(): Result<Unit> = withContext(Dispatchers.Default) {
        val requestArray =
            WashProgramRequest(
                write = 1,
                startStop = 0,
                programNumber = 3,
                delayValue = 0
            ).toQueryString().toByteArray()

        Timber.d("Reset command (plain): ${requestArray.decodeToString()}")

        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Reset command (encrypted): $stringMessage")

        when (val result = writeData(stringMessage)) {
            is Result.Success -> {
                try {
                    val encryptedBytes = hexToBytes(result.data)
                    val decrypted = decrypt(xorKey, encryptedBytes)
                    Timber.d(
                        "Reset command response (decrypted): ${decrypted.decodeToString()}"
                    )
                    Result.Success(Unit)
                } catch (e: Throwable) {
                    Result.Error(ErrorReason.Unknown(e))
                }
            }

            is Result.Error -> Result.Error(result.reason)
        }
    }

    suspend fun getUsageStats(): Result<StatusCounters> = withContext(Dispatchers.Default) {
            val requestArray = WashProgramRequest(getStats = 1).toQueryString().toByteArray()

            Timber.d("Usage stats request: ${requestArray.decodeToString()}")
            val encrypted = encrypt(xorKey, requestArray)
            val stringMessage = bytesToHex(encrypted)
            Timber.d("Usage stats command (encrypted): $stringMessage")

            when (val result = writeData(stringMessage)) {
                is Result.Success -> {
                    try {
                        val encryptedBytes = hexToBytes(result.data)
                        val decrypted = decrypt(xorKey, encryptedBytes)
                        val jsonString = decrypted.decodeToString()
                        Timber.d("Usage stats command response (decrypted): $jsonString")

                        val usageStatsResponse =
                            json.decodeFromString<MachineUsageStatsResponse>(jsonString)

                        Result.Success(usageStatsResponse.statusCounters)
                    } catch (e: Throwable) {
                        Result.Error(ErrorReason.Unknown(e))
                    }
                }

                is Result.Error -> Result.Error(result.reason)
            }
        }

    private fun Throwable.toErrorReason(): ErrorReason {
        return when (this) {
            is ConnectException,
            is SocketTimeoutException,
            is IOException -> ErrorReason.NetworkError(this.localizedMessage ?: "Network error")
            else -> ErrorReason.Unknown(this)
        }
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
        }
    }
}