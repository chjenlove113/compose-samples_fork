package com.app.tintuccongnghe.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.glance.appwidget.updateAll
import com.app.tintuccongnghe.widget.RssWidget
import com.app.tintuccongnghe.wear.WearRssSyncPublisher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RssRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssRepository: com.app.tintuccongnghe.domain.repository.IRssRepository,
    private val wearRssSyncPublisher: WearRssSyncPublisher
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RssRefreshWorker", "Worker started...")
        return try {
            rssRepository.refreshRssItems()
            try {
                wearRssSyncPublisher.publishLatestItems()
            } catch (error: Exception) {
                Log.w("RssRefreshWorker", "Wear RSS sync is unavailable", error)
            }
            Log.d("RssRefreshWorker", "Finished RSS refresh")
            RssWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e("RssRefreshWorker", "Error in RSS refresh", e)
            Result.failure()
        }
    }
}
