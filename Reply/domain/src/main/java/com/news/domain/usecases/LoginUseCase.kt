package com.news.domain.usecases

import com.news.domain.models.LoginRequest
import com.news.domain.models.LoginResponse
import com.news.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): LoginResponse {
        return repository.login(request)
    }
}
