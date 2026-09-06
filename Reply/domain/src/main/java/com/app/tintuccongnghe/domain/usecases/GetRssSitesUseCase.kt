package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.repository.IRssRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRssSitesUseCase @Inject constructor(
    private val repository: IRssRepository
) {
    operator fun invoke(): Flow<List<AppUserSite>> {
        return repository.getRssSites()
    }
}
