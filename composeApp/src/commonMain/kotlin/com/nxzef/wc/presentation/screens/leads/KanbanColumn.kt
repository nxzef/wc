package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.toComposeColor
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus

private val PRESET_COLORS = listOf(
    "#F44336", "#FF9800", "#FFEB3B", "#4CAF50",
    "#2196F3", "#3F51B5", "#9C27B0", "#9E9E9E"
)

@Composable
fun KanbanColumn(
    status: LeadStatus,
    leads: List<Lead>,
    taskCounts: Map<String, Int>,
    draggingLeadId: String?,
    isHighlighted: Boolean,
    syncingLeadIds: Set<String> = emptySet(),
    isDraggable: Boolean = false,
    isSaving: Boolean = false,
    onLeadClick: (Lead) -> Unit,
    onDeleteStatus: (() -> Unit)?,
    onRenameStatus: (newName: String) -> Unit,
    onChangeColor: (newColor: String) -> Unit,
    onColumnDragStart: () -> Unit = {},
    onColumnDrag: (deltaX: Float) -> Unit = {},
    onColumnDragEnd: () -> Unit = {},
    onColumnDragCancel: () -> Unit = {},
    onDragStart: (lead: Lead, taskCount: Int, windowPos: Offset, size: IntSize) -> Unit,
    onDrag: (leadId: String, delta: Offset) -> Unit,
    onDragEnd: (leadId: String) -> Unit,
    onDragCancel: (leadId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = status.color.toComposeColor()

    var isHovered by remember { mutableStateOf(false) }
    var isColumnDragging by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var renameText by remember(status.name) { mutableStateOf(status.name) }

    val showMoreVert = isHovered || showMenu || showRenameDialog || showColorDialog || isColumnDragging

    // Always get latest callbacks to avoid stale captures in the long-running pointerInput coroutine
    val latestOnColumnDragStart by rememberUpdatedState(onColumnDragStart)
    val latestOnColumnDrag by rememberUpdatedState(onColumnDrag)
    val latestOnColumnDragEnd by rememberUpdatedState(onColumnDragEnd)
    val latestOnColumnDragCancel by rememberUpdatedState(onColumnDragCancel)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isHighlighted) color.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.medium
            )
            .then(
                if (isHighlighted) Modifier.border(2.dp, color, MaterialTheme.shapes.medium)
                else Modifier
            )
            .padding(16.dp)
    ) {
        // ── Column header ──────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 20.dp, start = 4.dp, end = 4.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> isHovered = true
                                PointerEventType.Exit -> isHovered = false
                                else -> {}
                            }
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, MaterialTheme.shapes.extraSmall)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = status.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = leads.size.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(Modifier.width(4.dp))

            // ── Drag handle (non-default columns only) ─────────────────────────
            if (isDraggable) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .pointerInput(status.id) {
                            detectDragGestures(
                                onDragStart = {
                                    isColumnDragging = true
                                    latestOnColumnDragStart()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    latestOnColumnDrag(dragAmount.x)
                                },
                                onDragEnd = {
                                    isColumnDragging = false
                                    latestOnColumnDragEnd()
                                },
                                onDragCancel = {
                                    isColumnDragging = false
                                    latestOnColumnDragCancel()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isSaving -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        showMoreVert -> Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── MoreVert menu ──────────────────────────────────────────────────
            Box(modifier = Modifier.size(28.dp)) {
                if (showMoreVert) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Status options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { showMenu = false; showRenameDialog = true },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change Color") },
                        onClick = { showMenu = false; showColorDialog = true },
                        leadingIcon = {
                            Icon(Icons.Default.Palette, null, modifier = Modifier.size(18.dp))
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = if (onDeleteStatus != null)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        onClick = {
                            if (onDeleteStatus != null) {
                                showMenu = false
                                onDeleteStatus()
                            }
                        },
                        enabled = onDeleteStatus != null,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete, null,
                                modifier = Modifier.size(18.dp),
                                tint = if (onDeleteStatus != null)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    )
                }
            }
        }

        // ── Lead cards ────────────────────────────────────────────────────────
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(leads, key = { it.id }) { lead ->
                val count = taskCounts[lead.id] ?: 0
                DraggableLeadCard(
                    lead = lead,
                    taskCount = count,
                    isDragging = lead.id == draggingLeadId,
                    isSyncing = lead.id in syncingLeadIds,
                    onClick = { onLeadClick(lead) },
                    onDragStart = { windowPos, size ->
                        onDragStart(lead, count, windowPos, size)
                    },
                    onDrag = { delta -> onDrag(lead.id, delta) },
                    onDragEnd = { onDragEnd(lead.id) },
                    onDragCancel = { onDragCancel(lead.id) }
                )
            }
        }
    }

    // ── Rename dialog ──────────────────────────────────────────────────────────
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false; renameText = status.name },
            title = { Text("Rename Status") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Status name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameText.trim()
                        if (trimmed.isNotEmpty()) {
                            onRenameStatus(trimmed)
                            showRenameDialog = false
                        }
                    },
                    enabled = renameText.trim().isNotEmpty() && renameText.trim() != status.name,
                    shape = MaterialTheme.shapes.medium
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false; renameText = status.name }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Color picker dialog ────────────────────────────────────────────────────
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Choose Color") },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PRESET_COLORS.forEach { hex ->
                        val swatch = hex.toComposeColor()
                        val isSelected = hex.equals(status.color, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(swatch, CircleShape)
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable {
                                    onChangeColor(hex)
                                    showColorDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) { Text("Close") }
            }
        )
    }
}
