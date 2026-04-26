package com.adrianosilva.simply_works.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.adrianosilva.simply_works.data.local.KeyManager
import com.adrianosilva.simply_works.data.remote.CandyApiService
import com.adrianosilva.simply_works.domain.Result
import com.adrianosilva.simply_works.domain.models.MachineState
import com.adrianosilva.simply_works.ui.notifications.NotificationHelper
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WashProgramWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("WashProgramWorker: Starting background check for wash program.")

        // We instantiate CandyApiService directly here using KeyManager and hardcoded URL,
        // similar to how it is done in MainActivity.
        val keyManager = KeyManager(appContext)
        val xorKey = keyManager.getKey() ?: "".toByteArray()
        val savedIp = keyManager.getIp()

        if (xorKey.isEmpty()) {
            Timber.e("WashProgramWorker: XOR key is empty. Cannot check status.")
            return Result.failure()
        }
        
        if (savedIp.isNullOrEmpty()) {
            Timber.e("WashProgramWorker: IP is empty. Cannot check status.")
            return Result.failure()
        }

        val candyApiService = CandyApiService(baseUrl = "http://$savedIp", xorKey = xorKey)

        return when (val statusResult = candyApiService.getStatus()) {
            is com.adrianosilva.simply_works.domain.Result.Success -> {
                val status = statusResult.data
                val state = status.machineState
                val remainingTimeMinutes = status.remainingTimeMinutes

                Timber.d("WashProgramWorker: Status fetched. State=$state, RemainingTime=$remainingTimeMinutes")

                if (state == MachineState.FINISHED1 || state == MachineState.FINISHED2 || remainingTimeMinutes == 0 && state != MachineState.ERROR) {
                    Timber.d("WashProgramWorker: Program finished! Sending notification.")
                    NotificationHelper.showWashFinishedNotification(appContext)
                    Result.success()
                } else if (state == MachineState.RUNNING || state == MachineState.DELAYED_START_PROGRAMMED || state == MachineState.DELAYED_START_SELECTION) {
                    // Still running, schedule the next check
                    scheduleNextCheck(appContext, remainingTimeMinutes)
                    Result.success()
                } else {
                    // IDLE, PAUSED, ERROR, etc., we probably don't need to keep checking in the background
                    // unless we want to notify on Error.
                    Timber.d("WashProgramWorker: State is $state. Stopping background checks.")
                    Result.success()
                }
            }
            is com.adrianosilva.simply_works.domain.Result.Error -> {
                Timber.e("WashProgramWorker: Failed to get status: ${statusResult.reason}")
                // Retry if network error
                Result.retry()
            }
        }
    }

    private fun scheduleNextCheck(context: Context, remainingTimeMinutes: Int) {
        // We use a heuristic for the next check.
        // If remaining time is > 5 minutes, we schedule it for (remainingTimeMinutes - 1) minutes.
        // If it's <= 5 minutes, we schedule it to check in 2 minutes.
        // If it's 1 minute, check in 1 minute.
        val delayMinutes = when {
            remainingTimeMinutes > 5 -> remainingTimeMinutes - 1
            remainingTimeMinutes > 2 -> 2
            else -> 1
        }.toLong()
        
        // Ensure delay is at least 1 minute
        val safeDelay = maxOf(1L, delayMinutes)

        Timber.d("WashProgramWorker: Scheduling next check in $safeDelay minutes.")

        val nextWork = OneTimeWorkRequestBuilder<WashProgramWorker>()
            .setInitialDelay(safeDelay, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, androidx.work.ExistingWorkPolicy.REPLACE, nextWork)
    }

    companion object {
        const val WORK_NAME = "WashProgramTrackingWorker"

        fun startTracking(workManager: WorkManager, initialDelayMinutes: Int = 1) {
            val safeDelay = maxOf(1L, initialDelayMinutes.toLong())
            val workRequest = OneTimeWorkRequestBuilder<WashProgramWorker>()
                .setInitialDelay(safeDelay, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()

            workManager.enqueueUniqueWork(WORK_NAME, androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}
