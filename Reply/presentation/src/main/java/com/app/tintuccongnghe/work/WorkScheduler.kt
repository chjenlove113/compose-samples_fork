package com.app.tintuccongnghe.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val RSS_WORK_NAME = "RssRefreshWorker"

    fun scheduleRssRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshRequest = PeriodicWorkRequestBuilder<RssRefreshWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RSS_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Use UPDATE to refresh settings if they changed
            refreshRequest
        )
    }

    fun refreshRssNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshRequest = OneTimeWorkRequestBuilder<RssRefreshWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RSS_WORK_NAME + "_manual",
            ExistingWorkPolicy.REPLACE,
            refreshRequest
        )
    }
}
