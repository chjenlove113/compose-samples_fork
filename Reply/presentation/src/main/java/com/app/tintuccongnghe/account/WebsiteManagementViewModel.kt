package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.toDomain
import com.app.tintuccongnghe.domain.models.NewsWebsite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteManagementViewModel @Inject constructor(
    appDatabase: AppDatabase
) : ViewModel() {
    private val dao = appDatabase.newsWebsiteDao()

    val websites: StateFlow<List<NewsWebsite>> = dao.getAllWebsites()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateWebsiteActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            dao.updateActiveStatus(id, isActive)
        }
    }
}
