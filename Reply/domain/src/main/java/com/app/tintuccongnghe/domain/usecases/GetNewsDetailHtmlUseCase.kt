package com.app.tintuccongnghe.domain.usecases

import com.app.tintuccongnghe.domain.repository.INewsDetailRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsDetailHtmlUseCase @Inject constructor(
    private val repository: INewsDetailRepository
) {
    operator fun invoke(
        link: String,
        cat: String,
        id: Int,
        html: String,
        slug: String
    ): Flow<Result<String>> {
        return repository.fetchNewsDetailHtml(link, cat, id, html, slug)
    }
}
