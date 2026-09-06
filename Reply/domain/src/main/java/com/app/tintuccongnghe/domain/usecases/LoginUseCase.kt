package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): LoginResponse {
        return repository.login(request)
    }
}
