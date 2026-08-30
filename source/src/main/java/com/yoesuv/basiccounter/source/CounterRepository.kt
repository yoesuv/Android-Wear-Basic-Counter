package com.yoesuv.basiccounter.source

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class CounterRepository(
    context: Context,
) : DataClient.OnDataChangedListener {
    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)
    private val nodeClient = Wearable.getNodeClient(appContext)
    private val _counter = MutableLiveData(0)
    private val lock = Any()

    private val reconcileHandler = Handler(Looper.getMainLooper())

    private var nodeId: String? = null
    private val pendingDeltas = mutableListOf<Int>()

    private val nodeDeltas = mutableMapOf<String, Int>()

    private var started = false

    val counter: LiveData<Int> = _counter

    fun start() {
        if (started) return
        dataClient.addListener(this)
        started = true
        scheduleReconcile()
        nodeClient.localNode
            .addOnSuccessListener { node ->
                flushPending(node.id)
                requestCurrentState()
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # resolve local node failed", it)
            }
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
        reconcileHandler.removeCallbacksAndMessages(null)
        started = false
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { dataEvent ->
            val path = dataEvent.dataItem.uri.path ?: return@forEach
            if (!path.startsWith(Constants.DELTA_PREFIX)) return@forEach

            val remoteNode = path.removePrefix(Constants.DELTA_PREFIX)
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    val delta =
                        String(dataEvent.dataItem.data ?: return@forEach, StandardCharsets.UTF_8)
                            .toIntOrNull()
                            ?: return@forEach
                    ingest(remoteNode, delta)
                }

                DataEvent.TYPE_DELETED -> {
                    removeNode(remoteNode)
                }
            }
        }
        reconcileAgainstKnownNodes()
    }

    private fun applyLocalDelta(delta: Int) {
        val id = synchronized(lock) { nodeId }
        if (id == null) {
            synchronized(lock) { pendingDeltas.add(delta) }
            return
        }
        val nextLocal =
            synchronized(lock) {
                val current = nodeDeltas[id] ?: 0
                nodeDeltas[id] = current + delta
                recompute()
            }
        _counter.value = nextLocal
        broadcast(id, nodeDeltas[id] ?: 0)
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

    private fun removeNode(node: String) {
        val next =
            synchronized(lock) {
                if (nodeDeltas.remove(node) == null) return@synchronized null
                recompute()
            }
        next?.let { _counter.postValue(it) }
    }

    private fun flushPending(id: String) {
        val cumulative =
            synchronized(lock) {
                nodeId = id
                if (pendingDeltas.isEmpty()) {
                    recompute()
                } else {
                    val base = nodeDeltas[id] ?: 0
                    nodeDeltas[id] = base + pendingDeltas.sum()
                    pendingDeltas.clear()
                    recompute()
                }
            }
        _counter.value = cumulative
        broadcast(id, nodeDeltas[id] ?: 0)
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
            .getDataItems(uri, DataClient.FILTER_PREFIX)
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
                reconcileAgainstKnownNodes()
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # load state failed", it)
            }
    }

    private fun scheduleReconcile() {
        reconcileHandler.postDelayed(
            object : Runnable {
                override fun run() {
                    if (!started) return
                    reconcileAgainstKnownNodes()
                    reconcileHandler.postDelayed(this, RECONCILE_INTERVAL_MS)
                }
            },
            RECONCILE_INTERVAL_MS,
        )
    }

    private fun reconcileAgainstKnownNodes() {
        nodeClient.connectedNodes
            .addOnSuccessListener { known ->
                val knownIds = known.map { it.id }.toSet()
                val stale =
                    synchronized(lock) {
                        nodeDeltas.keys.filter { it != nodeId && it !in knownIds }
                    }
                stale.forEach { node ->
                    removeNode(node)
                    deleteDeltaForNode(node)
                }
            }.addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # reconcile failed", it)
            }
    }

    private fun deleteDeltaForNode(node: String) {
        val uri = ("wear://*" + Constants.DELTA_PREFIX + node).toUri()
        dataClient
            .deleteDataItems(uri, DataClient.FILTER_PREFIX)
            .addOnFailureListener {
                Log.e(Constants.TAG_ERROR, "CounterRepository # delete stale delta failed", it)
            }
    }

    private companion object {
        const val RECONCILE_INTERVAL_MS = 30_000L
    }
}
