package com.adrianosilva.simply_works.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.adrianosilva.simply_works.domain.usecase.NetworkScanner
import com.adrianosilva.simply_works.ui.notifications.NotificationHelper
import com.adrianosilva.simply_works.ui.settings.SettingsScreenRoot
import com.adrianosilva.simply_works.ui.settings.SettingsViewModel
import com.adrianosilva.simply_works.ui.setup.SetupScreenRoot
import com.adrianosilva.simply_works.ui.setup.SetupViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val keyManager = KeyManager(applicationContext)
        val savedKey = keyManager.getKey()
        val xorKey = savedKey ?: "".toByteArray()

        val savedIp = keyManager.getIp()
        var candyApiService by mutableStateOf<CandyApiService?>(null)

        if (savedIp != null) {
            val service = CandyApiService(baseUrl = "http://$savedIp", xorKey = xorKey)
            candyApiService = service
            if (xorKey.isEmpty()) {
                service.deriveKey { derived -> keyManager.saveKey(derived) }
            }
        }
        
        NotificationHelper.createNotificationChannel(applicationContext)
        
        setContent {
            SimplyworksTheme {
                val navController = rememberNavController()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Timber.d("Notification permission granted: $isGranted")
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = if (savedIp != null) Screen.MachineStatus.route else Screen.Setup.route,
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
                            },
                            popEnterTransition = {
                                slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
                            },
                            popExitTransition = {
                                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
                            }
                    ) {
                        composable(Screen.Setup.route) {
                            val networkScanner = NetworkScanner(applicationContext)
                            val setupViewModel: SetupViewModel = viewModel(
                                factory = SetupViewModel.Companion.SetupViewModelFactory(keyManager, networkScanner)
                            )
                            SetupScreenRoot(
                                viewModel = setupViewModel,
                                onSetupComplete = { ip ->
                                    val newService = CandyApiService(baseUrl = "http://$ip", xorKey = xorKey)
                                    candyApiService = newService
                                    if (xorKey.isEmpty()) {
                                        newService.deriveKey { derived -> keyManager.saveKey(derived) }
                                    }
                                    navController.navigate(Screen.MachineStatus.route) {
                                        popUpTo(Screen.Setup.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.MachineStatus.route) {
                            candyApiService?.let { api ->
                                val workManager = androidx.work.WorkManager.getInstance(applicationContext)
                                MachineStatusScreenRoot(
                                    viewModel =
                                            viewModel(
                                                    factory =
                                                            MachineStatusViewModel.Companion
                                                                    .MachineStatusViewModelFactory(
                                                                            api, workManager
                                                                    )
                                            ),
                                    onGoToWashProgram = {
                                        navController.navigate(Screen.WashProgram.route)
                                        Timber.d("Navigating to Wash Program Screen")
                                    },
                                    onGoToUsageStats = {
                                        navController.navigate(Screen.UsageStats.route)
                                        Timber.d("Navigating to Usage Stats Screen")
                                    },
                                    onGoToSettings = {
                                        navController.navigate(Screen.Settings.route)
                                        Timber.d("Navigating to Settings Screen")
                                    }
                            )
                            }
                        }

                        composable(Screen.WashProgram.route) {
                            candyApiService?.let { api ->
                                WashProgramScreenRoot(
                                    viewModel =
                                            viewModel(
                                                    factory =
                                                            WashProgramViewModel.Companion
                                                                    .WashProgramViewModelFactory(
                                                                            api, keyManager
                                                                    )
                                            ),
                                    onWashStarted = {
                                        navController.popBackStack()
                                        Timber.d("Wash started, navigating back")
                                    }
                            )
                            }
                        }

                        composable(Screen.UsageStats.route) {
                            candyApiService?.let { api ->
                                UsageStatsScreenRoot(
                                    viewModel(
                                            factory =
                                                    UsageStatsViewModel.Companion
                                                            .UsageStatsViewModelFactory(
                                                                    api
                                                            )
                                    )
                            )
                            }
                        }

                        composable(Screen.Settings.route) {
                            val networkScanner = NetworkScanner(applicationContext)
                            val settingsViewModel: SettingsViewModel = viewModel(
                                factory = SettingsViewModel.Companion.SettingsViewModelFactory(keyManager, networkScanner)
                            )
                            SettingsScreenRoot(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() },
                                onIpUpdated = {
                                    val newIp = keyManager.getIp()
                                    if (newIp != null) {
                                        val newService = CandyApiService(baseUrl = "http://$newIp", xorKey = xorKey)
                                        candyApiService = newService
                                        if (xorKey.isEmpty()) {
                                            newService.deriveKey { derived -> keyManager.saveKey(derived) }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
