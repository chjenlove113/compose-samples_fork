package com.news.domain.usecases

import com.news.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
