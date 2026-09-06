package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.LoginResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthInfoUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    operator fun invoke(): Flow<LoginResponse?> {
        return repository.getAuthInfo()
    }
}
