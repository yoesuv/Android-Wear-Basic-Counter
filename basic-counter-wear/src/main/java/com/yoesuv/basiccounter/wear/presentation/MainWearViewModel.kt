package com.yoesuv.basiccounter.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yoesuv.basiccounter.source.CounterRepository

class MainWearViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = CounterRepository(application)

    val counter = repository.counter

    init {
        repository.start()
    }

    fun add() {
        repository.add()
    }

    fun subtract() {
        repository.subtract()
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}
