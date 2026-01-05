package com.adrianosilva.simply_works.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adrianosilva.simply_works.data.local.KeyManager
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.ui.machinestatus.MachineStatusScreenRoot
import com.adrianosilva.simply_works.ui.machinestatus.MachineStatusViewModel
import com.adrianosilva.simply_works.ui.theme.SimplyworksTheme
import com.adrianosilva.simply_works.ui.usagestats.UsageStatsScreenRoot
import com.adrianosilva.simply_works.ui.usagestats.UsageStatsViewModel
import com.adrianosilva.simply_works.ui.washprogram.WashProgramScreenRoot
import com.adrianosilva.simply_works.ui.washprogram.WashProgramViewModel
import timber.log.Timber

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val keyManager = KeyManager(applicationContext)
        val savedKey = keyManager.getKey()
        val xorKey = savedKey ?: "".toByteArray()
        val candyApiService = CandyApiService(baseUrl = "http://192.168.1.185", xorKey = xorKey) // TODO: Make machine IP configurable

        if (xorKey.isEmpty()) {
            candyApiService.deriveKey { derived ->
                keyManager.saveKey(derived)
            }
        }
        setContent {
            SimplyworksTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = Screen.MachineStatus.route
                    ) {
                        composable(Screen.MachineStatus.route) {
                            MachineStatusScreenRoot(
                                viewModel = viewModel(
                                    factory = MachineStatusViewModel.Companion.MachineStatusViewModelFactory(candyApiService)
                                ),
                                onGoToWashProgram = {
                                    navController.navigate(Screen.WashProgram.route)
                                    Timber.d("Navigating to Wash Program Screen")
                                },
                                onGoToUsageStats = {
                                    navController.navigate(Screen.UsageStats.route)
                                    Timber.d("Navigating to Usage Stats Screen")
                                }
                            )
                        }

                        composable(Screen.WashProgram.route) {
                            WashProgramScreenRoot(
                                viewModel = viewModel(
                                    factory = WashProgramViewModel.Companion.WashProgramViewModelFactory(candyApiService)
                                ),
                                onWashStarted = {
                                    navController.popBackStack()
                                    Timber.d("Wash started, navigating back")
                                }
                            )
                        }

                        composable(Screen.UsageStats.route) {
                            UsageStatsScreenRoot(
                                viewModel(
                                    factory = UsageStatsViewModel.Companion.UsageStatsViewModelFactory(candyApiService)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
