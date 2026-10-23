package com.lumo.store.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RateLimitState {
    private val _events = MutableSharedFlow<RateLimitException>(extraBufferCapacity = 1)
    val events: SharedFlow<RateLimitException> = _events.asSharedFlow()

    fun notify(ex: RateLimitException) {
        _events.tryEmit(ex)
    }
}