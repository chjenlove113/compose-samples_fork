package com.news.domain.usecases

import com.news.domain.models.AppUserSite
import com.news.domain.models.AppUserSiteRequest
import com.news.domain.repository.IAppUserSiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppUserSiteListUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    operator fun invoke(request: AppUserSiteRequest): Flow<List<AppUserSite>> {
        return repository.getAppUserSiteList(request)
    }
}
