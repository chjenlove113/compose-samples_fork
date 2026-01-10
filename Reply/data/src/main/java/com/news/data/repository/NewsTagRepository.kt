package com.news.data.repository

import android.util.Log
import com.news.data.api.NewsTagService
import com.news.data.local.IAppDbService
import com.news.data.mappers.toNewsTagEntity
import com.news.data.mappers.toNewsTagModel
import com.news.data.models.PostBaseModel
import com.news.domain.mappers.toNewsTag
import com.news.domain.models.NewsTag
import com.news.domain.repository.INewsTagRepository
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
            emit(newsTagService.fetchNewsTagList(PostBaseModel("0x010000005AF87A9E1F172D1C4F960C68530762CA93C92B23E402335D")))
        }.map { it.body()?.map {
            it.toNewsTag() } ?: emptyList() }
            .flatMapConcat { newsTagList -> flow {
                emit(appDbService.deleteAllAndInsertAllNewsTag((newsTagList.map { it.toNewsTagEntity() }))) } }
            .flatMapConcat { appDbService.getNewsTag().map { it.map { it.toNewsTagModel() } }}
    }
}