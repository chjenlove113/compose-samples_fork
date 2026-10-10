package com.app.tintuccongnghe.wear

import android.content.Context
import com.app.tintuccongnghe.domain.repository.IRssRepository
import com.app.tintuccongnghe.domain.wear.WearRssSyncProtocol
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearRssSyncPublisher @Inject constructor(
    @ApplicationContext context: Context,
    private val rssRepository: IRssRepository
) {
    private val dataClient = Wearable.getDataClient(context)

    suspend fun publishLatestItems() = withContext(Dispatchers.IO) {
        val payload = WearRssSyncProtocol.encode(rssRepository.fetchLatestRssItems())
        val request = PutDataRequest.create(WearRssSyncProtocol.ITEMS_PATH)
            .setData(payload)
            .setUrgent()
        Tasks.await(dataClient.putDataItem(request))
    }
}
