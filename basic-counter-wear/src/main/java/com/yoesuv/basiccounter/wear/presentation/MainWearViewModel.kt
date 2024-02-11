package com.yoesuv.basiccounter.wear.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.yoesuv.basiccounter.source.Constants.COUNT_PATH
import com.yoesuv.basiccounter.source.Constants.TAG_DEBUG
import com.yoesuv.basiccounter.source.Constants.TAG_ERROR

class MainWearViewModel : ViewModel(), DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient
    var counter: MutableLiveData<Int> = MutableLiveData(0)

    fun init(context: Context) {
        dataClient = Wearable.getDataClient(context)
        dataClient.addListener(this)
    }

    fun add() {
        val current = counter.value ?: 0
        counter.value = current + 1
        send()
    }

    fun subtract() {
        val current = counter.value ?: 0
        counter.value = current - 1
        send()
    }

    private fun send() {
        val counter = this.counter.value ?: 0
        val request = PutDataRequest.create(COUNT_PATH)
        request.setData("$counter".toByteArray())
        dataClient.putDataItem(request.setUrgent()).addOnSuccessListener {
            Log.d(TAG_DEBUG, "MainWearViewModel # SUCCESS SEND DATA ${it.data}")
        }.addOnFailureListener {
            Log.e(TAG_ERROR, "MainWearViewModel # ERROR $it")
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG_DEBUG, "MainWearViewModel # onDataChanged: $dataEvents")
        dataEvents.forEach { dataEvent ->
            if (dataEvent.dataItem.data != null) {
                val uri = dataEvent.dataItem.uri
                if (uri.path == COUNT_PATH) {
                    val data = dataEvent.dataItem.data!!
                    val strCount = String(data, Charsets.UTF_8)
                    val intCount = strCount.toInt()
                    Log.d(TAG_DEBUG, "MainWearViewModel # onDataChanged: $strCount")
                    counter.postValue(intCount)
                }
            }
        }
    }

}