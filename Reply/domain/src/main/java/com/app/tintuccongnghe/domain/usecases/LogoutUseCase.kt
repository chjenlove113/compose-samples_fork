package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
