package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.IAuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.deleteAccount()
    }
}
