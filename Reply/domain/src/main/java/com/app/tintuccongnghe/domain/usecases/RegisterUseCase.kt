package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.RegisterRequest
import com.app.tintuccongnghe.domain.models.RegisterResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(request: RegisterRequest): RegisterResponse {
        return repository.register(request)
    }
}
