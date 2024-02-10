package com.yoesuv.basiccounter

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.yoesuv.basiccounter.source.Constants
import java.nio.charset.StandardCharsets

class MainViewModel: ViewModel(), DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient
    var counter: MutableLiveData<Int> = MutableLiveData(0)

    fun init(context: Context) {
        dataClient = Wearable.getDataClient(context)
        dataClient.addListener(this)
    }

    fun add() {
        val current = counter.value ?: 0
        counter.postValue(current + 1)
    }

    fun subtract() {
        val current = counter.value ?: 0
        counter.postValue(current - 1)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { dataEvent ->
            if (dataEvent.dataItem.data != null) {
                val uri = dataEvent.dataItem.uri
                if (uri.path == Constants.COUNT_PATH) {
                    val data = dataEvent.dataItem.data!!
                    val strCount = String(data, StandardCharsets.UTF_8)
                    Log.d(Constants.TAG_DEBUG, "MainViewModel # onDataChanged: $strCount")
                }
            }
        }
    }

}