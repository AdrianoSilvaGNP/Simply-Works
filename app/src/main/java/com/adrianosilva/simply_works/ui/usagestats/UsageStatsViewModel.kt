package com.adrianosilva.simply_works.ui.usagestats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.domain.Result
import kotlinx.coroutines.launch
import timber.log.Timber

class UsageStatsViewModel(private val api: CandyApiService): ViewModel() {

    var state by mutableStateOf(UsageStatsUiState())
        private set

    init {
        loadUsageStats()
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            when (val result = api.getUsageStats()) {
                is Result.Success -> {
                    val statusCounters = result.data
                    val stats: MutableList<Pair<String, Int>> = mutableListOf()

                    // using reflection to not hardcode each field
                    statusCounters.javaClass.declaredFields.forEach {
                        try {
                            val name = it.name
                            it.isAccessible = true // Ensure we can access it
                            val value = (it.get(statusCounters) as String).toInt()

                            stats.add(Pair(name, value))
                            Timber.d("Usage stat - $name: $value")
                        } catch (e: Exception) {
                            Timber.e(e, "Error getting usage stat for field ${it.name}")
                        }
                    }
                    state = state.copy(stats = stats)
                }

                is Result.Error -> {
                    Timber.e("Error fetching usage stats: ${result.reason}")
                }
            }
        }
    }

    companion object {
        class UsageStatsViewModelFactory(private val apiService: CandyApiService):
            ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(UsageStatsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST") return UsageStatsViewModel(apiService) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
