package com.app.tintuccongnghe.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.data.local.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class NewsMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            val newsJson = remoteMessage.data["news_data"]
            if (newsJson != null) {
                try {
                    val news = Json.decodeFromString<News>(newsJson)
                    NotificationHelper.showNotification(this, news, appDatabase)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing news data", e)
                }
            }
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "NewsMessagingService"
    }
}
