package com.app.tintuccongnghe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.app.tintuccongnghe.work.WorkScheduler
import com.app.tintuccongnghe.work.Work2Scheduler
import com.app.tintuccongnghe.domain.usecases.GetDailyRefreshSettingsUseCase
import com.app.tintuccongnghe.domain.usecases.GetRefreshIntervalUseCase
import com.app.tintuccongnghe.domain.usecases.GetRefreshStrategyUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var getRefreshIntervalUseCase: GetRefreshIntervalUseCase
    
    @Inject
    lateinit var getDailyRefreshSettingsUseCase: GetDailyRefreshSettingsUseCase

    @Inject
    lateinit var getRefreshStrategyUseCase: GetRefreshStrategyUseCase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        MobileAds.initialize(this) {}
        
        CoroutineScope(Dispatchers.Main).launch {
            val strategy = getRefreshStrategyUseCase().first()
            
            if (strategy == "INTERVAL") {
                Work2Scheduler.cancelAllDailyRefreshes(this@NewsApplication)
                val interval = getRefreshIntervalUseCase().first()
                WorkScheduler.scheduleRssRefresh(this@NewsApplication, interval)
            } else {
                WorkScheduler.cancelRefresh(this@NewsApplication)
                val dailySettings = getDailyRefreshSettingsUseCase().first()
                if (dailySettings.enabled) {
                    Work2Scheduler.scheduleDailyRssRefreshes(this@NewsApplication, dailySettings.times)
                }
            }
        }
    }
}
