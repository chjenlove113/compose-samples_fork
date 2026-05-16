package com.news.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news.domain.models.LoginResponse
import com.news.domain.usecases.GetAuthInfoUseCase
import com.news.domain.usecases.GetNightModeUseCase
import com.news.domain.usecases.LogoutUseCase
import com.news.domain.usecases.SetNightModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    getAuthInfoUseCase: GetAuthInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    getNightModeUseCase: GetNightModeUseCase,
    private val setNightModeUseCase: SetNightModeUseCase
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

    fun toggleNightMode(enabled: Boolean) {
        viewModelScope.launch {
            setNightModeUseCase(enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
