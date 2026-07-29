package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.usecases.DeleteAccountUseCase
import com.app.tintuccongnghe.domain.usecases.GetAuthInfoUseCase
import com.app.tintuccongnghe.domain.usecases.GetFontScaleUseCase
import com.app.tintuccongnghe.domain.usecases.GetNightModeUseCase
import com.app.tintuccongnghe.domain.usecases.LogoutUseCase
import com.app.tintuccongnghe.domain.usecases.SetFontScaleUseCase
import com.app.tintuccongnghe.domain.usecases.SetLanguageUseCase
import com.app.tintuccongnghe.domain.usecases.SetNightModeUseCase
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
    private val setLanguageUseCase: SetLanguageUseCase
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
