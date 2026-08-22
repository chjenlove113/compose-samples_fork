package com.app.tintuccongnghe.work

import android.content.Context
import androidx.work.*
import com.app.tintuccongnghe.domain.models.RefreshTime
import java.util.*
import java.util.concurrent.TimeUnit

object Work2Scheduler {
    private const val DAILY_RSS_WORK_PREFIX = "DailyRssRefreshWorker_"

    fun scheduleDailyRssRefreshes(context: Context, times: List<RefreshTime>) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel all existing daily work first to avoid orphans if times list changed
        cancelAllDailyRefreshes(context)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        times.forEach { time ->
            val initialDelay = calculateDelay(time.hour, time.minute)
            val uniqueName = "${DAILY_RSS_WORK_PREFIX}${time.hour}_${time.minute}"

            val refreshRequest = PeriodicWorkRequestBuilder<RssRefreshWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .addTag(DAILY_RSS_WORK_PREFIX) // Use tag for easy bulk cancellation
                .build()

            workManager.enqueueUniquePeriodicWork(
                uniqueName,
                ExistingPeriodicWorkPolicy.UPDATE,
                refreshRequest
            )
        }
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis - now
    }
    
    fun cancelAllDailyRefreshes(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_RSS_WORK_PREFIX)
    }
}
