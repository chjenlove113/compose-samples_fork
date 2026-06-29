package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.models.ShowHomeDataModel
import com.app.tintuccongnghe.domain.repository.IShowHomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowHomeUseCase @Inject constructor(private val showHomeRepository: IShowHomeRepository) {
    operator fun invoke(page: Int = 1, site_Slug: String = "", userId: String? = null): Flow<ShowHomeDataModel> {
        return showHomeRepository.getShowHome(page, site_Slug, userId)
    }
}