package com.yoesuv.basiccounter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    var counter: MutableLiveData<Int> = MutableLiveData(0)

}