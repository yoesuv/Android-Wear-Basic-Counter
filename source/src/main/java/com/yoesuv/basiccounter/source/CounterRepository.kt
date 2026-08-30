package com.yoesuv.basiccounter.source

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class CounterRepository(
    context: Context,
) : DataClient.OnDataChangedListener {
    private val dataClient = Wearable.getDataClient(context.applicationContext)
    private val _counter = MutableLiveData(0)
    private var started = false

    val counter: LiveData<Int> = _counter

    fun start() {
        if (started) return
        dataClient.addListener(this)
        started = true
    }

    fun add() {
        setCounter((_counter.value ?: 0) + 1)
    }

    fun subtract() {
        setCounter((_counter.value ?: 0) - 1)
    }

    fun close() {
        if (!started) return
        dataClient.removeListener(this)
        started = false
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { dataEvent ->
            if (dataEvent.type != DataEvent.TYPE_CHANGED) return@forEach
            if (dataEvent.dataItem.uri.path != Constants.COUNT_PATH) return@forEach

            val data = dataEvent.dataItem.data ?: return@forEach
            val count =
                String(data, StandardCharsets.UTF_8).toIntOrNull()
                    ?: return@forEach

            if (_counter.value != count) {
                Log.d(Constants.TAG_DEBUG, "CounterRepository # received: $count")
                _counter.postValue(count)
            }
        }
    }

    private fun setCounter(value: Int) {
        _counter.value = value

        val request = PutDataRequest.create(Constants.COUNT_PATH)
        request.data = value.toString().toByteArray(StandardCharsets.UTF_8)
        dataClient
            .putDataItem(request.setUrgent())
            .addOnSuccessListener {
                Log.d(Constants.TAG_DEBUG, "CounterRepository # sent: $value")
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # send failed", it)
            }
    }
}
