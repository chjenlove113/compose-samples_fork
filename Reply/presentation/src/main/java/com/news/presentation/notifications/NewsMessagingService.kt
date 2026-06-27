package com.news.presentation.notifications

import android.util.Log
/*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.news.domain.models.News
import kotlinx.serialization.json.Json

/**
 * Skeleton for Firebase Messaging Service.
 * To use this, add 'com.google.firebase:firebase-messaging' dependency
 * and register this service in AndroidManifest.xml.
 */
class NewsMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            // Assuming the 'news' object is sent as a JSON string in the 'news_data' field
            val newsJson = remoteMessage.data["news_data"]
            if (newsJson != null) {
                try {
                    val news = Json.decodeFromString<News>(newsJson)
                    NotificationHelper.showNotification(this, news)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing news data", e)
                }
            }
        }

        // Also check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Send token to your server if needed
    }

    companion object {
        private const val TAG = "NewsMessagingService"
    }
}
*/
