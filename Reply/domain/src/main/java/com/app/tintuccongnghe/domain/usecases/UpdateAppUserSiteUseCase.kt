package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.repository.IAppUserSiteRepository
import javax.inject.Inject

class UpdateAppUserSiteUseCase @Inject constructor(
    private val repository: IAppUserSiteRepository
) {
    suspend operator fun invoke(site: AppUserSite): Boolean {
        return repository.updateAppUserSite(site)
    }
}
