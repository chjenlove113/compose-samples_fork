package com.example.reply.wear.rss

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.RssItem
import com.app.tintuccongnghe.domain.repository.IRssRepository
import com.app.tintuccongnghe.domain.usecases.GetLatestRssItemsUseCase
import com.app.tintuccongnghe.domain.usecases.GetRssItemsForSiteUseCase
import com.app.tintuccongnghe.domain.wear.WearRssSyncProtocol
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RssViewModel @Inject constructor(
    getLatestRssItemsUseCase: GetLatestRssItemsUseCase,
    getRssItemsForSiteUseCase: GetRssItemsForSiteUseCase,
    private val rssRepository: IRssRepository,
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel(), DataClient.OnDataChangedListener {
    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    private val siteId: Int? = savedStateHandle.get<String>("siteId")?.toIntOrNull()
    private val siteGroup: String? = savedStateHandle["siteGroup"]
    private val siteKind: String? = savedStateHandle["siteKind"]
    val siteName: String = savedStateHandle["siteName"] ?: "Tin tức"

    val uiState: StateFlow<List<RssItem>> = (if (siteId != null && siteGroup != null && siteKind != null) {
        getRssItemsForSiteUseCase(siteId, siteGroup, siteKind)
    } else {
        getLatestRssItemsUseCase()
    }).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        dataClient.addListener(this)
        loadCachedPhoneItems()
        requestPhoneItems()
    }

    fun refresh() {
        requestPhoneItems()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == WearRssSyncProtocol.ITEMS_PATH
            ) {
                event.dataItem.data?.let(::updatePhoneItems)
            }
        }
    }

    override fun onCleared() {
        dataClient.removeListener(this)
        super.onCleared()
    }

    private fun loadCachedPhoneItems() {
        dataClient.dataItems
            .addOnSuccessListener { dataItems ->
                try {
                    dataItems.firstOrNull {
                        it.uri.path == WearRssSyncProtocol.ITEMS_PATH
                    }?.data?.let(::updatePhoneItems)
                } finally {
                    dataItems.release()
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to read cached phone RSS items", error)
            }
    }

    private fun requestPhoneItems() {
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    messageClient.sendMessage(
                        node.id,
                        WearRssSyncProtocol.REQUEST_PATH,
                        byteArrayOf()
                    ).addOnFailureListener { error ->
                        Log.w(TAG, "Unable to request RSS items from ${node.displayName}", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to find the paired phone", error)
            }
    }

    private fun updatePhoneItems(payload: ByteArray) {
        try {
            val items = WearRssSyncProtocol.decode(payload)
            viewModelScope.launch {
                try {
                    rssRepository.saveSyncedRssItems(items)
                    Log.d(TAG, "Saved ${items.size} phone RSS items to the Wear database")
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to save phone RSS items to the Wear database", error)
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "Unable to decode phone RSS items", error)
        }
    }

    private companion object {
        const val TAG = "RssViewModel"
    }
}
