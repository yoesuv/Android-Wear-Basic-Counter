package com.yoesuv.basiccounter.listener

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.yoesuv.basiccounter.source.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets

class MobileCounterWearListener : WearableListenerService() {

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            if (uri.path == Constants.COUNT_PATH) {
                scope.launch {
                    try {
                        val nodeId = uri.host!!
                        val payload = uri.toString().toByteArray()
                        val strPayload = String(payload, StandardCharsets.UTF_8)
                        Log.d(Constants.TAG_DEBUG, "MobileCounterWearListener # payload : $strPayload")
                        messageClient.sendMessage(nodeId, Constants.DATA_ITEM_RECEIVED_PATH, payload).await()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }

            }
        }
    }

}