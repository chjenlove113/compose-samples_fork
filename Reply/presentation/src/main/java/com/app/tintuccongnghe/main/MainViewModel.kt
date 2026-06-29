package com.app.tintuccongnghe.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.usecases.GetFontScaleUseCase
import com.app.tintuccongnghe.domain.usecases.GetNightModeUseCase
import com.app.tintuccongnghe.domain.usecases.SetFontScaleUseCase
import kotlinx.serialization.Serializable
import com.app.tintuccongnghe.domain.models.News
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Serializable
data class NewsNotificationPayload(
    val news: News,
    val tabKey: String? = null
)

data class RssJump(val siteId: Int, val siteGroup: String, val timestamp: Long)

@HiltViewModel
class MainViewModel @Inject constructor(
    getNightModeUseCase: GetNightModeUseCase,
    getFontScaleUseCase: GetFontScaleUseCase,
    private val setFontScaleUseCase: SetFontScaleUseCase
) : ViewModel() {

    private val _rssJump = MutableStateFlow<RssJump?>(null)
    val rssJump = _rssJump.asStateFlow()

    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews = _selectedNews.asStateFlow()

    private val _targetTab = MutableStateFlow<String?>(null)
    val targetTab = _targetTab.asStateFlow()

    fun selectNews(news: News?, tabKey: String? = null) {
        _selectedNews.value = news
        _targetTab.value = tabKey
    }

    fun clearTargetTab() {
        _targetTab.value = null
    }

    fun setTargetTab(tabKey: String?) {
        _targetTab.value = tabKey
    }

    fun setRssJump(siteId: Int, siteGroup: String) {
        _rssJump.update { RssJump(siteId, siteGroup, System.currentTimeMillis()) }
    }

    fun clearRssJump() {
        _rssJump.update { null }
    }

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

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            setFontScaleUseCase(scale)
        }
    }
}
