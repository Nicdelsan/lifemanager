package com.lifemanager.common

import kotlin.reflect.KClass

/**
 * Abstracts periodic/one-off background work so feature modules never depend on
 * WorkManager (or any other platform scheduler) directly.
 */
interface BackgroundScheduler {
    fun schedulePeriodic(request: PeriodicWorkRequest)
    fun scheduleOneOff(request: OneOffWorkRequest)
    fun cancel(uniqueName: String)
}

data class PeriodicWorkRequest(
    val uniqueName: String,
    val workerClass: KClass<*>,
    val repeatIntervalMinutes: Long,
    val requiresNetwork: Boolean = false,
    val requiresCharging: Boolean = false,
)

data class OneOffWorkRequest(
    val uniqueName: String,
    val workerClass: KClass<*>,
    val initialDelayMinutes: Long = 0,
    val requiresNetwork: Boolean = false,
)
