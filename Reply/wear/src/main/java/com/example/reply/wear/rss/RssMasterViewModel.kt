package com.example.reply.wear.rss

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tintuccongnghe.domain.models.AppUserSite
import com.app.tintuccongnghe.domain.usecases.GetRssSitesUseCase
import com.app.tintuccongnghe.domain.wear.WearRssSyncProtocol
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RssMasterViewModel @Inject constructor(
    getRssSitesUseCase: GetRssSitesUseCase,
    @ApplicationContext context: Context
) : ViewModel() {
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    val uiState: StateFlow<List<AppUserSite>> = getRssSitesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        requestPhoneItems()
    }

    fun refresh() {
        requestPhoneItems()
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

    private companion object {
        const val TAG = "RssMasterViewModel"
    }
}
