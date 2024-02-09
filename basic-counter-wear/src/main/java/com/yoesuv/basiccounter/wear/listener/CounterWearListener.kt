package com.yoesuv.basiccounter.wear.listener

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.yoesuv.basiccounter.wear.data.COUNT_PATH
import com.yoesuv.basiccounter.wear.data.DATA_ITEM_RECEIVED_PATH
import com.yoesuv.basiccounter.wear.data.TAG_DEBUG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets

class CounterWearListener : WearableListenerService() {

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            if (uri.path == COUNT_PATH) {
                scope.launch {
                    try {
                        val nodeId = uri.host!!
                        val payload = uri.toString().toByteArray()
                        val strPayload = String(payload, StandardCharsets.UTF_8)
                        Log.d(TAG_DEBUG, "CounterWearListener # payload : $strPayload")
                        messageClient.sendMessage(nodeId, DATA_ITEM_RECEIVED_PATH, payload).await()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }

            }
        }
    }

}