package com.app.tintuccongnghe.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.app.tintuccongnghe.main.MainActivity
import com.app.tintuccongnghe.presentation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reports a user-initiated RSS sync as an Android 17 Live Update. */
@Singleton
class RssSyncLiveUpdate @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class Session internal constructor(internal val notificationId: Int)

    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun start(totalFeeds: Int, feedName: String? = null, syncKey: Int? = null): Session {
        val session = Session(
            syncKey?.let { SITE_NOTIFICATION_ID_BASE + Math.floorMod(it, SITE_NOTIFICATION_ID_RANGE) }
                ?: NOTIFICATION_ID
        )
        if (!canNotify()) return session
        createChannel()
        postProgress(
            session = session,
            completedFeeds = 0,
            totalFeeds = totalFeeds,
            feedName = feedName ?: context.getString(R.string.rss_sync_preparing)
        )
        return session
    }

    fun update(session: Session, completedFeeds: Int, totalFeeds: Int, feedName: String) {
        if (!canNotify()) return
        postProgress(session, completedFeeds, totalFeeds, feedName)
    }

    fun complete(session: Session, totalFeeds: Int, newItems: Int, failedFeeds: Int = 0) {
        if (!canNotify()) return
        val title = if (failedFeeds == 0) {
            context.getString(R.string.rss_sync_complete)
        } else {
            context.getString(R.string.rss_sync_complete_with_errors)
        }
        val text = context.getString(
            R.string.rss_sync_result,
            totalFeeds - failedFeeds,
            totalFeeds,
            newItems
        )
        notificationManager.notify(
            session.notificationId,
            terminalNotification(session, title, text, failed = failedFeeds > 0)
        )
    }

    fun failed(session: Session, message: String? = null) {
        if (!canNotify()) return
        notificationManager.notify(
            session.notificationId,
            terminalNotification(
                session,
                context.getString(R.string.rss_sync_failed),
                message ?: context.getString(R.string.rss_sync_failed_description),
                failed = true
            )
        )
    }

    fun cancel(session: Session) {
        notificationManager.cancel(session.notificationId)
    }

    private fun postProgress(
        session: Session,
        completedFeeds: Int,
        totalFeeds: Int,
        feedName: String
    ) {
        val safeTotal = totalFeeds.coerceAtLeast(1)
        val safeCompleted = completedFeeds.coerceIn(0, safeTotal)
        val text = context.getString(
            R.string.rss_sync_progress,
            safeCompleted,
            totalFeeds.coerceAtLeast(0),
            feedName
        )
        val notification = if (Build.VERSION.SDK_INT >= ANDROID_17_API) {
            liveUpdateNotification(session, safeCompleted, safeTotal, text)
        } else {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_refresh)
                .setContentTitle(context.getString(R.string.rss_sync_in_progress))
                .setContentText(text)
                .setContentIntent(contentIntent(session))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setProgress(safeTotal, safeCompleted, totalFeeds <= 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
        notificationManager.notify(session.notificationId, notification)
    }

    @RequiresApi(ANDROID_17_API)
    private fun liveUpdateNotification(
        session: Session,
        completedFeeds: Int,
        totalFeeds: Int,
        text: String
    ): Notification {
        val progressStyle = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(completedFeeds)
            .setProgressIndeterminate(false)
            .setProgressSegments(
                listOf(Notification.ProgressStyle.Segment(totalFeeds))
            )

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle(context.getString(R.string.rss_sync_in_progress))
            .setContentText(text)
            .setContentIntent(contentIntent(session))
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setStyle(progressStyle)
            .setShortCriticalText("$completedFeeds/$totalFeeds")
            .setRequestPromotedOngoing(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun terminalNotification(
        session: Session,
        title: String,
        text: String,
        failed: Boolean
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent(session))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setTimeoutAfter(RESULT_VISIBILITY_MILLIS)
            .setPriority(if (failed) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun contentIntent(session: Session): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "reply://rss_feed".toUri(),
            context,
            MainActivity::class.java
        )
        return PendingIntent.getActivity(
            context,
            session.notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.rss_sync_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.rss_sync_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canNotify(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val ANDROID_17_API = 37
        const val CHANNEL_ID = "rss_sync"
        const val NOTIFICATION_ID = 17037
        const val SITE_NOTIFICATION_ID_BASE = 18000
        const val SITE_NOTIFICATION_ID_RANGE = 10_000
        const val RESULT_VISIBILITY_MILLIS = 5_000L
    }
}
