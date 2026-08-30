package com.yoesuv.basiccounter.source

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets
import androidx.core.net.toUri

class CounterRepository(
    context: Context,
) : DataClient.OnDataChangedListener {
    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)
    private val _counter = MutableLiveData(0)
    private val lock = Any()

    private val nodeId =
        Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: "unknown"

    private val nodeDeltas = mutableMapOf<String, Int>()

    private var started = false

    val counter: LiveData<Int> = _counter

    fun start() {
        if (started) return
        dataClient.addListener(this)
        started = true
        requestCurrentState()
    }

    fun add() {
        applyLocalDelta(+1)
    }

    fun subtract() {
        applyLocalDelta(-1)
    }

    fun close() {
        if (!started) return
        dataClient.removeListener(this)
        started = false
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { dataEvent ->
            if (dataEvent.type != DataEvent.TYPE_CHANGED) return@forEach
            val path = dataEvent.dataItem.uri.path ?: return@forEach
            if (!path.startsWith(Constants.DELTA_PREFIX)) return@forEach

            val delta =
                String(dataEvent.dataItem.data ?: return@forEach, StandardCharsets.UTF_8)
                    .toIntOrNull()
                    ?: return@forEach

            val remoteNode = path.removePrefix(Constants.DELTA_PREFIX)
            ingest(remoteNode, delta)
        }
    }

    private fun applyLocalDelta(delta: Int) {
        val nextLocal =
            synchronized(lock) {
                val current = nodeDeltas[nodeId] ?: 0
                nodeDeltas[nodeId] = current + delta
                recompute()
            }
        _counter.value = nextLocal
        broadcast(nodeId, nodeDeltas[nodeId] ?: 0)
    }

    private fun ingest(
        node: String,
        delta: Int,
    ) {
        val next =
            synchronized(lock) {
                if (nodeDeltas[node] == delta) return@synchronized null
                nodeDeltas[node] = delta
                recompute()
            }
        next?.let {
            Log.d(Constants.TAG_DEBUG, "CounterRepository # applied delta from $node -> $it")
            _counter.postValue(it)
        }
    }

    private fun recompute(): Int = nodeDeltas.values.sum()

    private fun broadcast(
        node: String,
        delta: Int,
    ) {
        val request = PutDataRequest.create(Constants.DELTA_PREFIX + node)
        request.data = delta.toString().toByteArray(StandardCharsets.UTF_8)
        dataClient
            .putDataItem(request.setUrgent())
            .addOnSuccessListener {
                Log.d(Constants.TAG_DEBUG, "CounterRepository # sent delta $delta for $node")
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # send delta failed", it)
            }
    }

    private fun requestCurrentState() {
        val uri = "wear://*${Constants.DELTA_PREFIX}".toUri()
        dataClient
            .getDataItems(uri)
            .addOnSuccessListener { buffer ->
                buffer.use { items ->
                    items.forEach { item ->
                        val path = item.uri.path ?: return@forEach
                        if (!path.startsWith(Constants.DELTA_PREFIX)) return@forEach
                        val delta =
                            String(item.data ?: return@forEach, StandardCharsets.UTF_8)
                                .toIntOrNull()
                                ?: return@forEach
                        val node = path.removePrefix(Constants.DELTA_PREFIX)
                        ingest(node, delta)
                    }
                }
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # load state failed", it)
            }
    }
}
