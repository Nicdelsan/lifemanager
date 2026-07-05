package com.lifemanager.common

import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Wraps WorkManager behind [BackgroundScheduler] so feature modules only ever see
 * the platform-agnostic contract. Never used for polling: every request carries
 * explicit constraints and a unique name to avoid duplicate scheduling.
 */
class WorkManagerBackgroundScheduler(
    private val workManager: WorkManager,
) : BackgroundScheduler {

    override fun schedulePeriodic(request: PeriodicWorkRequest) {
        val workRequest = androidx.work.PeriodicWorkRequest.Builder(
            request.workerClass.asCoroutineWorkerClass(),
            request.repeatIntervalMinutes,
            TimeUnit.MINUTES,
        )
            .setConstraints(request.toConstraints())
            .build()

        workManager.enqueueUniquePeriodicWork(
            request.uniqueName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    override fun scheduleOneOff(request: OneOffWorkRequest) {
        val workRequest = OneTimeWorkRequest.Builder(request.workerClass.asCoroutineWorkerClass())
            .setInitialDelay(request.initialDelayMinutes, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(if (request.requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .build()

        workManager.enqueueUniqueWork(
            request.uniqueName,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }

    override fun cancel(uniqueName: String) {
        workManager.cancelUniqueWork(uniqueName)
    }

    private fun PeriodicWorkRequest.toConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(if (requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
            .setRequiresCharging(requiresCharging)
            .build()

    @Suppress("UNCHECKED_CAST")
    private fun KClass<*>.asCoroutineWorkerClass(): Class<out CoroutineWorker> {
        require(CoroutineWorker::class.java.isAssignableFrom(this.java)) {
            "${this.qualifiedName} must extend CoroutineWorker to be scheduled via BackgroundScheduler."
        }
        return this.java as Class<out CoroutineWorker>
    }
}
