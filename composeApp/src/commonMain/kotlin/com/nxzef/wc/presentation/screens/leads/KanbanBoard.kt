package com.nxzef.wc.presentation.screens.leads

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import kotlin.math.roundToInt

@Composable
fun KanbanBoard(
    statuses: List<LeadStatus>,
    leads: List<Lead>,
    taskCounts: Map<String, Int>,
    filterStatusIds: Set<String>,
    isCompact: Boolean,
    columnWidth: Dp,
    horizontalPadding: Dp,
    syncingLeadIds: Set<String> = emptySet(),
    isReordering: Boolean = false,
    onReorderStatuses: (newOrder: List<LeadStatus>) -> Unit = {},
    onAddStatus: () -> Unit,
    onDeleteStatus: (LeadStatus) -> Unit,
    onRenameStatus: (statusId: String, newName: String) -> Unit,
    onChangeColor: (statusId: String, newColor: String) -> Unit,
    onLeadClick: (Lead) -> Unit,
    onStatusChange: (leadId: String, newStatusId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dragState = remember { KanbanDragState() }
    val columnBounds = remember { HashMap<String, LayoutCoordinates>() }
    var rootWindowPos by remember { mutableStateOf(Offset.Zero) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    var hoveredStatusId by remember { mutableStateOf<String?>(null) }

    // Column reorder drag state
    var colDragIdx by remember { mutableIntStateOf(-1) }
    var colDragOffsetX by remember { mutableFloatStateOf(0f) }

    val visibleStatuses = if (filterStatusIds.isEmpty()) statuses
    else statuses.filter { it.id in filterStatusIds }

    // Column reorder only available when no status filter is active
    val canReorder = filterStatusIds.isEmpty()

    val density = LocalDensity.current
    val gapPx = with(density) { (if (isCompact) 16.dp else 24.dp).toPx() }
    val colWidthPx = with(density) { columnWidth.toPx() }
    val colStepPx = colWidthPx + gapPx

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            rootWindowPos = coords.positionInWindow()
            boardSize = coords.size
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 24.dp)
        ) {
            Spacer(Modifier.width(horizontalPadding / 2))

            visibleStatuses.forEachIndexed { i, status ->
                key(status.id) {
                    val isDragged = i == colDragIdx
                    val n = visibleStatuses.size

                    // Compute where the dragged column is heading
                    val targetIdx = if (colDragIdx == -1 || n <= 1) i
                    else (colDragIdx + colDragOffsetX / colStepPx).roundToInt()
                        .coerceIn(1, n - 1)

                    // Each non-dragged column shifts by one step to make room
                    val displacementTarget = when {
                        colDragIdx == -1 || isDragged -> 0f
                        targetIdx > colDragIdx && i > colDragIdx && i <= targetIdx -> -colStepPx
                        targetIdx < colDragIdx && i >= targetIdx && i < colDragIdx -> colStepPx
                        else -> 0f
                    }
                    val displacement by animateFloatAsState(
                        targetValue = displacementTarget,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "colDisplace_$i"
                    )

                    KanbanColumn(
                        status = status,
                        leads = leads.filter { it.customStatus?.id == status.id },
                        taskCounts = taskCounts,
                        draggingLeadId = if (dragState.isDragging) dragState.lead?.id else null,
                        isHighlighted = hoveredStatusId == status.id,
                        syncingLeadIds = syncingLeadIds,
                        isDraggable = canReorder && !status.isDefault,
                        isSaving = isReordering,
                        onLeadClick = onLeadClick,
                        onDeleteStatus = if (status.isDefault) null else ({ onDeleteStatus(status) }),
                        onRenameStatus = { newName -> onRenameStatus(status.id, newName) },
                        onChangeColor = { newColor -> onChangeColor(status.id, newColor) },
                        onColumnDragStart = {
                            colDragIdx = i
                            colDragOffsetX = 0f
                        },
                        onColumnDrag = { dx -> colDragOffsetX += dx },
                        onColumnDragEnd = {
                            val savedIdx = colDragIdx
                            val savedOffset = colDragOffsetX
                            val nn = visibleStatuses.size
                            if (savedIdx != -1 && nn > 1) {
                                val computedTarget = (savedIdx + savedOffset / colStepPx)
                                    .roundToInt().coerceIn(1, nn - 1)
                                if (computedTarget != savedIdx) {
                                    val newOrder = visibleStatuses.toMutableList()
                                    val item = newOrder.removeAt(savedIdx)
                                    newOrder.add(computedTarget, item)
                                    onReorderStatuses(newOrder)
                                }
                            }
                            colDragIdx = -1
                            colDragOffsetX = 0f
                        },
                        onColumnDragCancel = {
                            colDragIdx = -1
                            colDragOffsetX = 0f
                        },
                        onDragStart = { lead, count, windowPos, size ->
                            dragState.start(lead, count, windowPos, size)
                        },
                        onDrag = { _, delta ->
                            dragState.onDrag(delta)
                            val center = dragState.cardCenter
                            var found = false
                            columnBounds.forEach { (sid, coords) ->
                                if (coords.isAttached && coords.boundsInWindow().contains(center)) {
                                    hoveredStatusId = sid
                                    found = true
                                }
                            }
                            if (!found) hoveredStatusId = null
                        },
                        onDragEnd = { leadId ->
                            val center = dragState.cardCenter
                            columnBounds.forEach { (sid, coords) ->
                                if (coords.isAttached && coords.boundsInWindow().contains(center)) {
                                    val currentStatusId =
                                        leads.find { it.id == leadId }?.customStatus?.id
                                    if (sid != currentStatusId) {
                                        onStatusChange(leadId, sid)
                                    }
                                }
                            }
                            dragState.reset()
                            hoveredStatusId = null
                        },
                        onDragCancel = { _ ->
                            dragState.reset()
                            hoveredStatusId = null
                        },
                        modifier = Modifier
                            .width(columnWidth)
                            .fillMaxHeight()
                            .offset {
                                IntOffset(
                                    x = (if (isDragged) colDragOffsetX else displacement).roundToInt(),
                                    y = 0
                                )
                            }
                            .zIndex(if (isDragged) 10f else 0f)
                            .then(
                                if (isDragged) Modifier.graphicsLayer {
                                    alpha = 0.92f
                                    shadowElevation = 20f
                                } else Modifier
                            )
                            .onGloballyPositioned { coords ->
                                columnBounds[status.id] = coords
                            }
                    )
                }
            }

            OutlinedButton(
                onClick = onAddStatus,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .width(160.dp)
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Status")
            }

            Spacer(Modifier.width(horizontalPadding / 2))
        }

        // ── Ghost card overlay ────────────────────────────────────────────────
        if (dragState.isDragging) {
            val draggingLead = dragState.lead
            if (draggingLead != null) {
                val rawLocalX = dragState.currentWindowPos.x - rootWindowPos.x
                val rawLocalY = dragState.currentWindowPos.y - rootWindowPos.y
                val cardH = dragState.cardSize.height.toFloat()
                val boardH = boardSize.height.toFloat()
                val localY = rubberBand(rawLocalY, 0f, (boardH - cardH).coerceAtLeast(0f))

                Box(
                    modifier = Modifier
                        .zIndex(999f)
                        .width(columnWidth)
                        .offset { IntOffset(rawLocalX.roundToInt(), localY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = 1.04f
                            scaleY = 1.04f
                            shadowElevation = 24f
                            alpha = 0.95f
                        }
                ) {
                    LeadCard(
                        lead = draggingLead,
                        taskCount = dragState.taskCount,
                        onClick = {},
                        elevation = 8.dp
                    )
                }
            }
        }
    }
}

private fun rubberBand(value: Float, min: Float, max: Float): Float = when {
    value < min -> min - (min - value) * 0.35f
    value > max -> max + (value - max) * 0.35f
    else -> value
}
