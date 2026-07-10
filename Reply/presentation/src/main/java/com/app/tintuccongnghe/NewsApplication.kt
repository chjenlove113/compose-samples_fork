package com.app.tintuccongnghe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.app.tintuccongnghe.work.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        MobileAds.initialize(this) {}
        
        WorkScheduler.scheduleRssRefresh(this)
    }
}
