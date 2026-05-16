package com.news.domain.usecases

import com.news.domain.models.RegisterRequest
import com.news.domain.models.RegisterResponse
import com.news.domain.repository.IAuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(request: RegisterRequest): RegisterResponse {
        return repository.register(request)
    }
}
