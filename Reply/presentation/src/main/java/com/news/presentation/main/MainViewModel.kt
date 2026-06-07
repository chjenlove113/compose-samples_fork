package com.news.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.usecases.GetFontScaleUseCase
import com.news.domain.usecases.GetNightModeUseCase
import com.news.domain.usecases.SetFontScaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RssJump(val siteId: Int, val siteGroup: String, val timestamp: Long)

@HiltViewModel
class MainViewModel @Inject constructor(
    getNightModeUseCase: GetNightModeUseCase,
    getFontScaleUseCase: GetFontScaleUseCase,
    private val setFontScaleUseCase: SetFontScaleUseCase
) : ViewModel() {

    private val _rssJump = MutableStateFlow<RssJump?>(null)
    val rssJump = _rssJump.asStateFlow()

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
