package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppUserSiteListUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    operator fun invoke(request: AppUserSiteRequest): Flow<List<AppUserSite>> {
        return repository.getAppUserSiteList(request)
    }
}
