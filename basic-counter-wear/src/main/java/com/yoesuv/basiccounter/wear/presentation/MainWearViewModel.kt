package com.yoesuv.basiccounter.wear.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainWearViewModel: ViewModel() {

    var counter: MutableLiveData<Int> = MutableLiveData(0)

    fun add() {
        val current = counter.value ?: 0
        counter.postValue(current + 1)
    }

    fun subtract() {
        val current = counter.value ?: 0
        counter.postValue(current - 1)
    }

}