package com.app.tintuccongnghe.data.repository

import androidx.paging.PagingData
import com.app.tintuccongnghe.data.api.NewsSiteService
import com.app.tintuccongnghe.data.local.IAppDbService
import com.app.tintuccongnghe.data.mappers.toNewsSiteEntity
import com.app.tintuccongnghe.data.mappers.toNewsSiteModel
import com.app.tintuccongnghe.domain.models.NewsSite
import com.app.tintuccongnghe.domain.repository.INewsSiteRepository
import com.app.tintuccongnghe.utils.AppContants
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

    override fun getListNewsSitePaging(): Flow<PagingData<NewsSite>> {
        TODO("Not yet implemented")
    }
}