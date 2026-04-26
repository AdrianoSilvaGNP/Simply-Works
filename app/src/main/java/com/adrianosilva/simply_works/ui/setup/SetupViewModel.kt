package com.adrianosilva.simply_works.ui.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.local.KeyManager
import com.adrianosilva.simply_works.domain.usecase.NetworkScanner
import kotlinx.coroutines.launch

class SetupViewModel(
    private val keyManager: KeyManager,
    private val networkScanner: NetworkScanner
) : ViewModel() {

    var ipAddress by mutableStateOf("")
        private set

    var isScanning by mutableStateOf(false)
        private set

    var scanError by mutableStateOf<String?>(null)
        private set

    fun onIpChanged(newIp: String) {
        ipAddress = newIp
        scanError = null
    }

    fun scanNetwork(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isScanning = true
            scanError = null
            
            val foundIp = networkScanner.scanNetwork()
            if (foundIp != null) {
                ipAddress = foundIp
                saveIpAndContinue(foundIp, onSuccess)
            } else {
                scanError = "Could not find washing machine on local network. Ensure you are connected to the same Wi-Fi."
            }
            
            isScanning = false
        }
    }

    fun saveIpAndContinue(ip: String, onSuccess: (String) -> Unit) {
        if (ip.isNotBlank()) {
            keyManager.saveIp(ip.trim())
            onSuccess(ip.trim())
        }
    }

    companion object {
        class SetupViewModelFactory(
            private val keyManager: KeyManager,
            private val networkScanner: NetworkScanner
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SetupViewModel::class.java)) {
                    return SetupViewModel(keyManager, networkScanner) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
