package com.news.domain.usecases

import com.news.domain.models.AppUserSite
import com.news.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class UpdateAppUserSiteUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(site: AppUserSite): Boolean {
        return repository.updateAppUserSite(site)
    }
}
