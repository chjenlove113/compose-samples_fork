package com.news.presentation.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.news.domain.models.News
import com.news.presentation.R
import com.news.presentation.main.MainActivity
import com.news.presentation.main.NewsNotificationPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NotificationHelper {
    private const val CHANNEL_ID = "news_notifications"
    private const val CHANNEL_NAME = "News Notifications"

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, news: News, tabKey: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new news articles"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create the payload
        val payload = NewsNotificationPayload(news = news, tabKey = tabKey)
        
        // Encode payload object to JSON and then to URL-safe string
        val newsJson = Uri.encode(Json.encodeToString(payload))
        
        // Use the deep link pattern defined in MainActivity
        val deepLinkUri = "reply://news_detail/$newsJson".toUri()
        
        val intent = Intent(
            Intent.ACTION_VIEW,
            deepLinkUri,
            context,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            news.Id, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(news.Title)
            .setContentText(news.ShortDes)
            .setStyle(NotificationCompat.BigTextStyle().bigText(news.ShortDes))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted. In a real app, you should request this in the UI.
                return
            }
        }
        
        NotificationManagerCompat.from(context).notify(news.Id, notification)
    }
}
