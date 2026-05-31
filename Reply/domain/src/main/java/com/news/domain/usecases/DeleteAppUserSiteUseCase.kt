package com.news.domain.usecases

import com.news.domain.models.AppUserSiteCreateRequest
import com.news.domain.models.AppUserSiteCreateResponse
import com.news.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class DeleteAppUserSiteUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(request: AppUserSiteCreateRequest): AppUserSiteCreateResponse {
        return repository.deleteAppUserSite(request)
    }
}
