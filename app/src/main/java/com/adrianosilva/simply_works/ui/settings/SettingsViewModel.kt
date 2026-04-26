package com.adrianosilva.simply_works.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.local.KeyManager
import com.adrianosilva.simply_works.domain.models.WashPreset
import com.adrianosilva.simply_works.domain.usecase.NetworkScanner
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val keyManager: KeyManager,
    private val networkScanner: NetworkScanner
) : ViewModel() {

    var ipAddress by mutableStateOf("")
        private set

    var isScanning by mutableStateOf(false)
        private set

    var scanMessage by mutableStateOf<String?>(null)
        private set

    var savedPresets by mutableStateOf<List<WashPreset>>(emptyList())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        ipAddress = keyManager.getIp() ?: ""
        savedPresets = keyManager.getPresets()
    }

    fun onIpChanged(newIp: String) {
        ipAddress = newIp
    }

    fun saveIp() {
        if (ipAddress.isNotBlank()) {
            keyManager.saveIp(ipAddress.trim())
            scanMessage = "IP Saved Successfully"
        }
    }

    fun scanNetwork() {
        viewModelScope.launch {
            isScanning = true
            scanMessage = null
            
            val foundIp = networkScanner.scanNetwork()
            if (foundIp != null) {
                ipAddress = foundIp
                keyManager.saveIp(foundIp)
                scanMessage = "Found machine at $foundIp! IP Saved."
            } else {
                scanMessage = "Could not find washing machine on local network."
            }
            
            isScanning = false
        }
    }

    fun deletePreset(preset: WashPreset) {
        val updated = savedPresets.filter { it != preset }
        keyManager.savePresets(updated)
        savedPresets = updated
    }

    companion object {
        class SettingsViewModelFactory(
            private val keyManager: KeyManager,
            private val networkScanner: NetworkScanner
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(keyManager, networkScanner) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
