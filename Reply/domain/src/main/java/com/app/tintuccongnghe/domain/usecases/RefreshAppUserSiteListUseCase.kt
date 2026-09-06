package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSiteRequest
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class RefreshAppUserSiteListUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(request: AppUserSiteRequest) {
        repository.refreshAppUserSiteList(request)
    }
}
