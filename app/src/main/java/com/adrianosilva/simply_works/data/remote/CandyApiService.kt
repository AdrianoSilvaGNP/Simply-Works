package com.adrianosilva.simply_works.data.remote


import android.util.Log
import com.adrianosilva.simply_works.MachineState
import com.adrianosilva.simply_works.Utils.bytesToHex
import com.adrianosilva.simply_works.Utils.decrypt
import com.adrianosilva.simply_works.Utils.encrypt
import com.adrianosilva.simply_works.Utils.hexToBytes
import com.adrianosilva.simply_works.WashProgramState
import com.adrianosilva.simply_works.WashingMachineStatus
import com.adrianosilva.simply_works.data.dto.StatusLavatrice
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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

    // Run Eco 30C 800RPM in 30 minutes
    suspend fun getTestRunEncrypted(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=321C071A085C5D413F1F341F5B5F4F2F0002380250534A371E250A565548391926010A0B50534A371E3813195B2B0A04455A5E435B514A33011B330C12535A5B433D220B1B350B13515B4138160A3D0C115356482211182A1F0056565648261B11231D055F5C5C41200A090C5B5A4F381103535E4B251E1E515B412E2253594D370B0D071D042503515B4138120F1B1F26060B0D06341C5A5C4D2302151E3D0E161A21005050") {
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun getStatusEncrypted(): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-read.json?encrypted=1") {
            accept(ContentType.Text.Html) // the machine reports text/html
        }.body()
    }

    suspend fun writeData(data: String): String = withContext(Dispatchers.IO) {
        client.get("$baseUrl/http-write.json?encrypted=1&data=$data") {
            accept(ContentType.Text.Html) // the machine reports text/html
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
        val encrypted = getStatusEncrypted()
        Log.d(TAG, "Encrypted data: $encrypted")

        val encryptedBytes = hexToBytes(encrypted)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Log.d(TAG, "Decrypted data: ${decrypted.decodeToString()}")

        val response = json.decodeFromString(StatusLavatrice.serializer(), decrypted.decodeToString())

        WashingMachineStatus(
            machineState = MachineState.fromCode(response.machineMode.toInt()),
            programState = WashProgramState.fromCode(response.programPhase.toInt()),
            program = response.programNumber.toInt(),
            temp = response.temp.toInt(),
            spinSpeed = response.spinSpeed.toInt() * 100,
            remainingMinutes = response.remainingTime.toInt()
        )
    }

    suspend fun callTestCycleIn30Min() = withContext(Dispatchers.Default) {
        val encrypted = getTestRunEncrypted()
        Log.d(TAG, "Test cycle command response (encrypted): $encrypted")

        val encryptedBytes = hexToBytes(encrypted)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Log.d(TAG, "Test cycle command response (decrypted): ${decrypted.decodeToString()}")
    }

    suspend fun callResetWashCycle() = withContext(Dispatchers.Default) {
        val encrypted = encrypt(xorKey, "Write=1&StSt=0&PrNm=3&DelVl=0".toByteArray())
        val stringMessage = bytesToHex(encrypted)
        Log.d(TAG, "Reset command (encrypted): $stringMessage")
        val response = writeData(stringMessage)

        val encryptedBytes = hexToBytes(response)
        val decrypted = decrypt(xorKey, encryptedBytes)
        Log.d(TAG, "Reset command response (decrypted): ${decrypted.decodeToString()}")
    }

    companion object {
        private const val TAG = "CandyApiService"
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
        }
    }

}
