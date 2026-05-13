package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.shared.util.DateUtils

// ─────────────────────────────────────────────────────────────────────────────
// Draggable wrapper — manages drag callbacks; shows placeholder while dragging
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DraggableLeadCard(
    lead: Lead,
    taskCount: Int,
    isDragging: Boolean,
    isSyncing: Boolean = false,
    onClick: () -> Unit,
    onDragStart: (windowPos: Offset, size: IntSize) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var windowPos by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // rememberUpdatedState ensures the pointerInput block always calls the latest lambdas
    val latestOnDragStart by rememberUpdatedState(onDragStart)
    val latestOnDrag by rememberUpdatedState(onDrag)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
    val latestOnDragCancel by rememberUpdatedState(onDragCancel)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                // Always track position; stop updating once this card is the active ghost
                if (!isDragging) {
                    windowPos = coords.positionInWindow()
                    cardSize = coords.size
                }
            }
            .pointerInput(lead.id) {
                detectDragGestures(
                    onDragStart = { latestOnDragStart(windowPos, cardSize) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        latestOnDrag(dragAmount)
                    },
                    onDragEnd = { latestOnDragEnd() },
                    onDragCancel = { latestOnDragCancel() }
                )
            }
    ) {
        if (isDragging) {
            DragPlaceholder(
                height = with(density) { cardSize.height.toDp() }.coerceAtLeast(56.dp)
            )
        } else {
            LeadCard(lead = lead, taskCount = taskCount, isSyncing = isSyncing, onClick = onClick)
        }
    }
}

@Composable
private fun DragPlaceholder(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                MaterialTheme.shapes.medium
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                MaterialTheme.shapes.medium
            )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Visual card — pure UI, no drag logic
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LeadCard(
    lead: Lead,
    taskCount: Int = 0,
    isSyncing: Boolean = false,
    onClick: () -> Unit,
    elevation: Dp = 0.dp
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (elevation == 0.dp)
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeadSourceBadge(source = lead.source)
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (lead.priority > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(lead.priority) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = WCTheme.colors.starGold
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = lead.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LeadInfoRow(
                    Icons.Default.CalendarMonth,
                    if (lead.eventDate != null)
                        DateUtils.formatDateRange(lead.eventDate!!, lead.eventEndDate)
                    else "Date not set"
                )
                LeadInfoRow(Icons.Default.Phone, lead.phone)
            }

            if (lead.eventType.name != "OTHER") {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = lead.eventType.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                color = if (taskCount > 0)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (taskCount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (taskCount > 0)
                            "$taskCount task${if (taskCount > 1) "s" else ""}"
                        else "No tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (taskCount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (lead.budget > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyUtils.formatINR(lead.budget),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeadInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
