package com.app.tintuccongnghe.account

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.usecases.LoginUseCase
import com.app.tintuccongnghe.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = loginUseCase(LoginRequest(Username = email.trim(), Password = password))
                if (response.Errors.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val errorMsg = response.Errors.firstOrNull()?.Description ?: "Login failed"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error") }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (!validateInput(email, password)) return
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = registerUseCase(RegisterRequest(Email = email.trim(), Password = password, UserName = email.trim()))
                if (response.Errors.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val errorMsg = response.Errors.joinToString { it.Description }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error") }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Invalid email format") }
            return false
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return false
        }
        return true
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
