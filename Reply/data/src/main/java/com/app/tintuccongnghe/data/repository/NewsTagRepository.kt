package com.app.tintuccongnghe.data.repository

import android.util.Log
import com.app.tintuccongnghe.data.api.NewsTagService
import com.app.tintuccongnghe.data.local.IAppDbService
import com.app.tintuccongnghe.data.mappers.toNewsTagEntity
import com.app.tintuccongnghe.data.mappers.toNewsTagModel
import com.app.tintuccongnghe.data.models.NewsByTagRequest
import com.app.tintuccongnghe.data.models.PostBaseModel
import com.app.tintuccongnghe.domain.mappers.toNewsTag
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.domain.models.NewsTag
import com.app.tintuccongnghe.domain.repository.INewsTagRepository
import com.app.tintuccongnghe.utils.AppContants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsTagRepository @Inject constructor(
    private val newsTagService: NewsTagService,
    private val appDbService: IAppDbService
) : INewsTagRepository {
    override fun getListNewsTag(): Flow<List<NewsTag>> {
        return flow {
            emit(newsTagService.fetchNewsTagList(PostBaseModel(AppContants.app_Id)))
        }.map { it.body()?.map {
            it.toNewsTag() } ?: emptyList() }
            .flatMapConcat { newsTagList -> flow {
                emit(appDbService.deleteAllAndInsertAllNewsTag((newsTagList.map { it.toNewsTagEntity() }))) } }
            .flatMapConcat { appDbService.getNewsTag().map { it.map { it.toNewsTagModel() } }}
    }

    override fun getNewsByTag(tagSlug: String, pageNumber: Int, rowsOfPage: Int): Flow<List<News>> {
        return flow {
            val response = newsTagService.fetchNewsByTag(NewsByTagRequest(tagSlug, "", pageNumber, rowsOfPage))
            emit(response.body() ?: emptyList())
        }
    }
}