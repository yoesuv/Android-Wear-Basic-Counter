package com.yoesuv.basiccounter.wear.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.yoesuv.basiccounter.wear.data.COUNT_PATH
import com.yoesuv.basiccounter.wear.data.TAG_DEBUG
import com.yoesuv.basiccounter.wear.data.TAG_ERROR

class MainWearViewModel: ViewModel() {

    private lateinit var dataClient: DataClient
    var counter: MutableLiveData<Int> = MutableLiveData(0)

    fun init(context: Context) {
        dataClient = Wearable.getDataClient(context)
    }

    fun add() {
        val current = counter.value ?: 0
        counter.postValue(current + 1)
        send()
    }

    fun subtract() {
        val current = counter.value ?: 0
        counter.postValue(current - 1)
        send()
    }

    private fun send () {
        val counter = this.counter.value ?: 0
        val request = PutDataRequest.create(COUNT_PATH)
        request.setData("$counter".toByteArray())
        dataClient.putDataItem(request.setUrgent()).addOnSuccessListener {
            Log.d(TAG_DEBUG,"MainWearViewModel # SUCCESS SEND DATA ${it.data}")
        }.addOnFailureListener {
            Log.e(TAG_ERROR, "MainWearViewModel # ERROR $it")
        }
    }

}