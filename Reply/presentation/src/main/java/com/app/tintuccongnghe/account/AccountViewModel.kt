package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.usecases.DeleteAccountUseCase
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.domain.usecases.GetDailyRefreshSettingsUseCase
import com.app.tintuccongnghe.domain.usecases.GetFontScaleUseCase
import com.app.tintuccongnghe.domain.usecases.GetNightModeUseCase
import com.app.tintuccongnghe.domain.usecases.GetRefreshIntervalUseCase
import com.app.tintuccongnghe.domain.usecases.GetRefreshStrategyUseCase
import com.app.tintuccongnghe.domain.usecases.LogoutUseCase
import com.app.tintuccongnghe.domain.usecases.SetDailyRefreshSettingsUseCase
import com.app.tintuccongnghe.domain.usecases.SetFontScaleUseCase
import com.app.tintuccongnghe.domain.usecases.SetLanguageUseCase
import com.app.tintuccongnghe.domain.usecases.SetNightModeUseCase
import com.app.tintuccongnghe.domain.usecases.SetRefreshIntervalUseCase
import com.app.tintuccongnghe.domain.usecases.SetRefreshStrategyUseCase
import com.app.tintuccongnghe.domain.models.DailyRefreshSettings
import com.app.tintuccongnghe.domain.models.RefreshTime
import com.app.tintuccongnghe.work.WorkScheduler
import com.app.tintuccongnghe.work.Work2Scheduler
import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    getAuthInfoUseCase: GetAuthInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    getNightModeUseCase: GetNightModeUseCase,
    private val setNightModeUseCase: SetNightModeUseCase,
    getFontScaleUseCase: GetFontScaleUseCase,
    private val setFontScaleUseCase: SetFontScaleUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    getRefreshIntervalUseCase: GetRefreshIntervalUseCase,
    private val setRefreshIntervalUseCase: SetRefreshIntervalUseCase,
    getDailyRefreshSettingsUseCase: GetDailyRefreshSettingsUseCase,
    private val setDailyRefreshSettingsUseCase: SetDailyRefreshSettingsUseCase,
    getRefreshStrategyUseCase: GetRefreshStrategyUseCase,
    private val setRefreshStrategyUseCase: SetRefreshStrategyUseCase,
    private val application: Application
) : ViewModel() {

    val authInfo: StateFlow<LoginResponse?> = getAuthInfoUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val nightMode: StateFlow<Boolean> = getNightModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val fontScale: StateFlow<Float> = getFontScaleUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    val refreshInterval: StateFlow<Long> = getRefreshIntervalUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 15L
        )

    val dailyRefreshSettings: StateFlow<DailyRefreshSettings> = getDailyRefreshSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyRefreshSettings(false, emptyList())
        )

    val refreshStrategy: StateFlow<String> = getRefreshStrategyUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "DAILY"
        )

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    fun toggleNightMode(enabled: Boolean) {
        viewModelScope.launch {
            setNightModeUseCase(enabled)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            setFontScaleUseCase(scale)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }

    fun setRefreshInterval(minutes: Long) {
        viewModelScope.launch {
            setRefreshIntervalUseCase(minutes)
            WorkScheduler.scheduleRssRefresh(application, minutes)
        }
    }

    fun setDailyRefreshSettings(enabled: Boolean, times: List<RefreshTime>) {
        viewModelScope.launch {
            setDailyRefreshSettingsUseCase(enabled, times)
            if (refreshStrategy.value == "DAILY") {
                if (enabled) {
                    Work2Scheduler.scheduleDailyRssRefreshes(application, times)
                } else {
                    Work2Scheduler.cancelAllDailyRefreshes(application)
                }
            }
        }
    }

    fun setRefreshStrategy(strategy: String) {
        viewModelScope.launch {
            setRefreshStrategyUseCase(strategy)
            // Re-schedule based on new strategy
            if (strategy == "DAILY") {
                WorkScheduler.cancelRefresh(application) // Need to add this
                if (dailyRefreshSettings.value.enabled) {
                    Work2Scheduler.scheduleDailyRssRefreshes(application, dailyRefreshSettings.value.times)
                }
            } else {
                Work2Scheduler.cancelAllDailyRefreshes(application)
                WorkScheduler.scheduleRssRefresh(application, refreshInterval.value)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val response = deleteAccountUseCase()
            _message.emit(response.Message ?: if (response.Success) "Account deleted successfully" else "Failed to delete account")
        }
    }
}
