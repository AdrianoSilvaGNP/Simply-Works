package com.adrianosilva.simply_works.data.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.adrianosilva.simply_works.Utils

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

    companion object {
        private const val KEY_XOR = "KEY_XOR"
    }
}
