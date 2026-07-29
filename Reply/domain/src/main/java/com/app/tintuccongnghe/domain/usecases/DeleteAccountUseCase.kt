package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.DeleteAccountResponse
import com.app.tintuccongnghe.domain.repository.IAuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(): DeleteAccountResponse {
        return repository.deleteAccount()
    }
}

