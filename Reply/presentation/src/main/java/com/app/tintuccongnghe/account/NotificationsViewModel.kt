package com.app.tintuccongnghe.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.data.local.AppDatabase
import com.app.tintuccongnghe.data.local.entities.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val appDatabase: AppDatabase
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = appDatabase.notificationDao()
        .getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            appDatabase.notificationDao().markAsRead(id)
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            appDatabase.notificationDao().deleteNotification(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            appDatabase.notificationDao().clearAll()
        }
    }
}
