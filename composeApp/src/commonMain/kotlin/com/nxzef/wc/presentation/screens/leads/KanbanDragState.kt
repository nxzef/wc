package com.nxzef.wc.presentation.screens.leads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.nxzef.wc.shared.model.Lead

internal class KanbanDragState {
    var isDragging by mutableStateOf(false)
        private set
    var lead by mutableStateOf<Lead?>(null)
        private set
    var taskCount by mutableIntStateOf(0)
        private set
    var initialWindowPos by mutableStateOf(Offset.Zero)
        private set
    var delta by mutableStateOf(Offset.Zero)
        private set
    var cardSize by mutableStateOf(IntSize.Zero)
        private set

    // Top-left corner of the ghost card in window coordinates
    val currentWindowPos: Offset get() = initialWindowPos + delta

    // Center of the ghost card — used for column hit-testing
    val cardCenter: Offset
        get() = currentWindowPos + Offset(cardSize.width / 2f, cardSize.height / 2f)

    fun start(lead: Lead, taskCount: Int, windowPos: Offset, size: IntSize) {
        this.lead = lead
        this.taskCount = taskCount
        this.initialWindowPos = windowPos
        this.delta = Offset.Zero
        this.cardSize = size
        this.isDragging = true
    }

    fun onDrag(amount: Offset) {
        delta += amount
    }

    fun reset() {
        isDragging = false
        lead = null
        delta = Offset.Zero
    }
}
