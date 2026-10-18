package com.v2ray.ang.handler

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrafficSnapshot(
    val upSpeed: Double = 0.0,
    val downSpeed: Double = 0.0,
    val upTotal: Long = 0L,
    val downTotal: Long = 0L,
)

/** Live proxy traffic, published by NotificationManager's speed loop (single reader of the core stats). */
object TrafficMonitor {
    private val _state = MutableStateFlow(TrafficSnapshot())
    val state: StateFlow<TrafficSnapshot> = _state.asStateFlow()

    fun publish(upSpeed: Double, downSpeed: Double, upDelta: Long, downDelta: Long) {
        val c = _state.value
        _state.value = TrafficSnapshot(upSpeed, downSpeed, c.upTotal + upDelta, c.downTotal + downDelta)
    }

    fun reset() {
        _state.value = TrafficSnapshot()
    }
}
