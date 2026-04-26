
package com.adrianosilva.simply_works.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.adrianosilva.simply_works.Utils
import androidx.core.content.edit

class KeyManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveKey(key: ByteArray) {
        val hexString = Utils.bytesToHex(key)
        sharedPreferences.edit { putString(KEY_XOR, hexString) }
    }

    fun getKey(): ByteArray? {
        val hexString = sharedPreferences.getString(KEY_XOR, null) ?: return null
        return try {
            Utils.hexToBytes(hexString)
        } catch (e: Exception) {
            null
        }
    }

    fun saveIp(ip: String) {
        sharedPreferences.edit { putString(KEY_IP, ip) }
    }

    fun getIp(): String? {
        return sharedPreferences.getString(KEY_IP, null)
    }

    fun savePresets(presets: List<com.adrianosilva.simply_works.domain.models.WashPreset>) {
        val json = kotlinx.serialization.json.Json.encodeToString(presets)
        sharedPreferences.edit { putString(KEY_PRESETS, json) }
    }

    fun getPresets(): List<com.adrianosilva.simply_works.domain.models.WashPreset> {
        val json = sharedPreferences.getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            kotlinx.serialization.json.Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_XOR = "KEY_XOR"
        private const val KEY_IP = "KEY_IP"
        private const val KEY_PRESETS = "KEY_PRESETS"
    }
}
