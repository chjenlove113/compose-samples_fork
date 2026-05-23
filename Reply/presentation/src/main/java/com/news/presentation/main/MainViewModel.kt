package com.news.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.usecases.GetFontScaleUseCase
import com.news.domain.usecases.GetNightModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getNightModeUseCase: GetNightModeUseCase,
    getFontScaleUseCase: GetFontScaleUseCase
) : ViewModel() {

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
}
