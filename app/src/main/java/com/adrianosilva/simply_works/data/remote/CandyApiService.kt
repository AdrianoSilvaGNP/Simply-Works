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

/**
 * Service class for interacting with the Candy washing machine API.
 *
 * @property baseUrl The base URL of the Candy washing machine API.
 * @property xorKey The XOR key used for encrypting and decrypting data.
 */
class CandyApiService(
    private val baseUrl: String,
    private var xorKey: ByteArray
) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 4000
            connectTimeoutMillis = 4000
        }
    }

    fun deriveKey() {
        if (xorKey.isEmpty()) {
            CoroutineScope(Dispatchers.Default).launch {
                val encrypted = readData()
                Timber.d("encrypted data: $encrypted")

                val encryptedBytes = hexToBytes(encrypted)
                val derivedKey = deriveKey(encryptedBytes)
                xorKey = derivedKey
                Timber.d("Derived XOR key: ${xorKey.toString(Charsets.UTF_8)}")
            }
        }
    }

    suspend fun readData(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-read.json?encrypted=1") {
            accept(ContentType.Text.Html)
        }.body()
    }

    suspend fun writeData(data: String): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=$data") {
            accept(ContentType.Text.Html)
        }.body()
    }

    suspend fun sendWashRequest(request: WashProgramRequest) {
        val requestArray = request.toQueryString().toByteArray()

        Timber.d("Wash program request: ${requestArray.decodeToString()}")
        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Wash program command (encrypted): $stringMessage")
        val response = writeData(stringMessage)

        val encryptedBytes = hexToBytes(response)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Timber.d("Wash program command response (decrypted): ${decrypted.decodeToString()}")
    }

    // Run Eco 30C 800RPM in 30 minutes
    suspend fun getTestRunEncrypted(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=321C071A085C5D413F1F341F5B5F4F2F0002380250534A371E250A565548391926010A0B50534A371E3813195B2B0A04455A5E435B514A33011B330C12535A5B433D220B1B350B13515B4138160A3D0C115356482211182A1F0056565648261B11231D055F5C5C41200A090C5B5A4F381103535E4B251E1E515B412E2253594D370B0D071D042503515B4138120F1B1F26060B0D06341C5A5C4D2302151E3D0E161A21005050") {
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun getUsageStatsEncrypted(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=220B1A3D1900185A5D") {// data is GetStat=1
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun callResetEncrypted(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=321C071A085C5D413F1F341F5B5E4F3B172003535E472802003D0B5656") {// data is Write=1&StSt=0&PrNm=3&DelVl=0
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun getStatus(): WashingMachineStatus = withContext(Dispatchers.Default) {
        val encrypted = readData()
        Timber.d("encrypted data: $encrypted")

        val encryptedBytes = hexToBytes(encrypted)
        val decrypted = decrypt(xorKey, encryptedBytes)
        val jsonString = decrypted.decodeToString()
        Timber.d("Decrypted data: $jsonString")

        val response = json.decodeFromString(MachineStatusResponse.serializer(), jsonString)

        val remainingTimeMinutes = response.statusLavatrice.remainingTime.toInt() / 60
        val hours = remainingTimeMinutes / 60
        val minutes = remainingTimeMinutes % 60

        val formattedTime = when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "$minutes min"
        }

        WashingMachineStatus(
            machineState = MachineState.fromCode(response.statusLavatrice.machineMode.toInt()),
            programState = WashProgramPhase.fromCode(response.statusLavatrice.programPhase.toInt()),
            program = WashProgram.allPrograms.first { it.number == response.statusLavatrice.programNumber.toInt() },
            temp = response.statusLavatrice.temp.toInt(),
            spinSpeed = response.statusLavatrice.spinSpeed.toInt() * 100,
            remainingTime = formattedTime,
            delayMinutes = response.statusLavatrice.delayValue.toInt()
        )
    }

    suspend fun callTestCycleIn30Min() = withContext(Dispatchers.Default) {

        val requestArray = WashProgramRequest(
            write = 1,
            startStop = 1,
            programNumber = 3,
            programCode = 2,
            delayValue = 1,
            targetTemperature = 30,
            targetSpinSpeed = 8,
        ).toQueryString().toByteArray()

        Timber.d("Test cycle request: ${requestArray.decodeToString()}")
        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Test cycle command (encrypted): $stringMessage")
        val response = writeData(stringMessage)

        val encryptedBytes = hexToBytes(response)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Timber.d("Test cycle command response (decrypted): ${decrypted.decodeToString()}")
    }

    suspend fun callResetWashCycle() = withContext(Dispatchers.Default) {

        val requestArray = WashProgramRequest(
            write = 1,
            startStop = 0,
            programNumber = 3,
            delayValue = 0
        ).toQueryString().toByteArray()

        Timber.d("Reset command (plain): ${requestArray.decodeToString()}")

        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Reset command (encrypted): $stringMessage")
        val response = writeData(stringMessage)

        val encryptedBytes = hexToBytes(response)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Timber.d("Reset command response (decrypted): ${decrypted.decodeToString()}")
    }

    suspend fun getUsageStats(): StatusCounters = withContext(Dispatchers.Default) {

        val requestArray = WashProgramRequest(
            getStats = 1
        ).toQueryString().toByteArray()

        Timber.d("Usage stats request: ${requestArray.decodeToString()}")
        val encrypted = encrypt(xorKey, requestArray)
        val stringMessage = bytesToHex(encrypted)
        Timber.d("Usage stats command (encrypted): $stringMessage")
        val response = writeData(stringMessage)

        val encryptedBytes = hexToBytes(response)
        val decrypted = decrypt(xorKey, encryptedBytes)
        val jsonString = decrypted.decodeToString()
        Timber.d("Usage stats command response (decrypted): $jsonString")

        val usageStatsResponse = json.decodeFromString(MachineUsageStatsResponse.serializer(), jsonString)

        usageStatsResponse.statusCounters
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
        }
    }
}