package com.news.domain.usecases

import com.news.domain.models.LoginResponse
import com.news.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthInfoUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    operator fun invoke(): Flow<LoginResponse?> {
        return repository.getAuthInfo()
    }
}
