package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSiteCopyRequest
import com.app.tintuccongnghe.domain.models.AppUserSiteCreateResponse
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class CopyAppUserSiteUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(request: AppUserSiteCopyRequest): AppUserSiteCreateResponse {
        return repository.copyAppUserSite(request)
    }
}
