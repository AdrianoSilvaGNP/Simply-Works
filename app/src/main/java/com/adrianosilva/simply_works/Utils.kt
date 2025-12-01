package com.adrianosilva.simply_works

object Utils {

    fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace("\\s".toRegex(), "")
        require(clean.length % 2 == 0)
        return ByteArray(clean.length / 2) { i ->
            ((clean[i * 2].digitToInt(16) shl 4) or clean[i * 2 + 1].digitToInt(16)).toByte()
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val intVal = byte.toInt() and 0xFF
            result.append(hexChars[intVal ushr 4])
            result.append(hexChars[intVal and 0x0F])
        }
        return result.toString()
    }

    // XOR decryption
    fun decrypt(key: ByteArray, encrypted: ByteArray): ByteArray {
        val out = ByteArray(encrypted.size)
        val keyLen = key.size

        for (i in encrypted.indices) {
            out[i] = (encrypted[i].toInt() xor key[i % keyLen].toInt()).toByte()
        }
        return out
    }

    // XOR encryption
    fun encrypt(key: ByteArray, plain: ByteArray): ByteArray {
        val out = ByteArray(plain.size)
        val keyLen = key.size

        for (i in plain.indices) {
            out[i] = (plain[i].toInt() xor key[i % keyLen].toInt()).toByte()
        }
        return out
    }

    // Key derivation (like brute_force_key)
    fun deriveKey(encrypted: ByteArray): ByteArray {
        val known = "{\r\n\t\"statusLavat".toByteArray(Charsets.ISO_8859_1) // first 16 bytes

        return ByteArray(16) { i ->
            (encrypted[i].toInt() xor known[i].toInt()).toByte()
        }
    }
}