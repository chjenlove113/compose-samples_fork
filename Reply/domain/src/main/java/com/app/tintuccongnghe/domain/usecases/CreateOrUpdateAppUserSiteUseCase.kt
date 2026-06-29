package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSiteCreateRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class CreateOrUpdateAppUserSiteUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return repository.createOrUpdateAppUserSite(request)
    }
}
