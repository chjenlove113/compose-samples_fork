package com.news.data.repository

import com.news.data.api.NewsSiteService
import com.news.data.local.IAppDbService
import com.news.data.mappers.toNewsSiteEntity
import com.news.data.mappers.toNewsSiteModel
import com.news.domain.models.NewsSite
import com.news.domain.repository.INewsSiteRepository
import com.news.utils.AppContants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class NewsSiteRepository @Inject constructor(private val newsSiteService: NewsSiteService,
                                             private val appDbService: IAppDbService) :
    INewsSiteRepository {
    override fun getListNewsSite(): Flow<List<NewsSite>> {
        return flow { emit(newsSiteService.fetchNewsSiteList(AppContants.app_Id)) }.map { it.body()?.dataModel ?: emptyList() }
            .flatMapConcat { newsSiteList -> flow {
                emit(appDbService.deleteAllAndInsertAllNewsSite((newsSiteList.map { it.toNewsSiteEntity() }))) } }
            .flatMapConcat { appDbService.getNewsSite().map { it.map { it.toNewsSiteModel() } }}
    }
}