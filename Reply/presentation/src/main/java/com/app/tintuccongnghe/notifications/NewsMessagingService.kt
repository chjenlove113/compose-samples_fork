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
            } else {
                remoteMessage.notification?.let { notification ->
                    val fallbackNews = News(
                        Id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                        Title = notification.title ?: "Notification",
                        ShortDes = notification.body ?: "",
                        Html = notification.body ?: "",
                        Date = "",
                        Source = "Push Notification"
                    )
                    NotificationHelper.showNotification(this, fallbackNews, appDatabase)
                }
            }
        } else {
            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "Message Notification Body: ${notification.body}")
                val fallbackNews = News(
                    Id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    Title = notification.title ?: "Notification",
                    ShortDes = notification.body ?: "",
                    Html = notification.body ?: "",
                    Date = "",
                    Source = "Push Notification"
                )
                NotificationHelper.showNotification(this, fallbackNews, appDatabase)
            }
        }


    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "NewsMessagingService"
    }
}
