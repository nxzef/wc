package com.nxzef.wc.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RefreshManager {
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshTrigger: SharedFlow<Unit> = _refreshTrigger.asSharedFlow()

    fun triggerRefresh() {
        _refreshTrigger.tryEmit(Unit)
    }
}
