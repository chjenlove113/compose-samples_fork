package com.app.tintuccongnghe.wear

import android.util.Log
import com.app.tintuccongnghe.domain.wear.WearRssSyncProtocol
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class RssWearSyncService : WearableListenerService() {
    @Inject
    lateinit var publisher: WearRssSyncPublisher

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearRssSyncProtocol.REQUEST_PATH) return

        try {
            runBlocking {
                publisher.publishLatestItems()
            }
        } catch (error: Exception) {
            Log.e(TAG, "Unable to publish RSS items to Wear OS", error)
        }
    }

    private companion object {
        const val TAG = "RssWearSyncService"
    }
}
