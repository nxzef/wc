package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.shared.model.Lead

const val COL_NAME = "list_name"
const val COL_STATUS = "list_status"
const val COL_DATE = "list_date"
const val COL_PHONE = "list_phone"
const val COL_EVENT = "list_event"
const val COL_SOURCE = "list_source"
const val COL_PRIORITY = "list_priority"

val LIST_COL_DEFAULTS = mapOf(
    COL_NAME to 200f,
    COL_STATUS to 130f,
    COL_DATE to 110f,
    COL_PHONE to 140f,
    COL_EVENT to 100f,
    COL_SOURCE to 110f,
    COL_PRIORITY to 90f
)

@Composable
fun LeadListView(
    leads: List<Lead>,
    onLeadClick: (Lead) -> Unit,
    columnWidths: Map<String, Float>,
    onColumnWidthChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val resizingWidths = remember { mutableStateMapOf<String, Float>() }

    fun colW(key: String): Dp =
        (resizingWidths[key] ?: columnWidths[key] ?: LIST_COL_DEFAULTS.getValue(key)).dp

    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(start = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(44.dp)) {
                    Text(
                        "#",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                listOf(
                    COL_NAME to "Name",
                    COL_STATUS to "Status",
                    COL_DATE to "Event Date",
                    COL_PHONE to "Phone",
                    COL_EVENT to "Event",
                    COL_SOURCE to "Source",
                    COL_PRIORITY to "Priority"
                ).forEach { (key, label) ->
                    val currentWidth =
                        resizingWidths[key] ?: columnWidths[key] ?: LIST_COL_DEFAULTS.getValue(key)
                    ResizableHeaderCell(
                        text = label,
                        widthDp = currentWidth,
                        onResizing = { resizingWidths[key] = it },
                        onResizeEnd = {
                            resizingWidths.remove(key)
                            onColumnWidthChange(key, it)
                        }
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (leads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.PersonSearch, null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "No leads match the current filters",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(leads, key = { _, lead -> lead.id }) { index, lead ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainerLowest
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onLeadClick(lead) }
                            .padding(start = 24.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(44.dp)) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(Modifier.width(colW(COL_NAME))) {
                            Text(
                                lead.fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(Modifier.width(colW(COL_STATUS))) {
                            LeadStatusBadge(
                                statusName = lead.statusName,
                                color = lead.customStatus?.color
                            )
                        }
                        Box(Modifier.width(colW(COL_DATE))) {
                            Text(
                                lead.eventDate ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(Modifier.width(colW(COL_PHONE))) {
                            Text(
                                lead.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(Modifier.width(colW(COL_EVENT))) {
                            Text(
                                lead.eventType.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(Modifier.width(colW(COL_SOURCE))) {
                            LeadSourceBadge(source = lead.source)
                        }
                        Box(Modifier.width(colW(COL_PRIORITY))) {
                            if (lead.priority > 0) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(lead.priority) {
                                        Icon(
                                            Icons.Default.Star, null,
                                            Modifier.size(13.dp),
                                            tint = StarGold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (index < leads.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResizableHeaderCell(
    text: String,
    widthDp: Float,
    minWidth: Float = 60f,
    maxWidth: Float = 500f,
    onResizing: (Float) -> Unit,
    onResizeEnd: (Float) -> Unit
) {
    val widthRef = rememberUpdatedState(widthDp)
    val onResizingRef = rememberUpdatedState(onResizing)
    val onResizeEndRef = rememberUpdatedState(onResizeEnd)
    var isResizing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(widthDp.dp).fillMaxHeight()) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.CenterStart).padding(end = 12.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(4.dp)
                .height(18.dp)
                .background(
                    MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = if (isResizing) 1f else 0.55f
                    ),
                    MaterialTheme.shapes.extraSmall
                )
                .pointerInput(Unit) {
                    var current = widthRef.value
                    detectDragGestures(
                        onDragStart = { isResizing = true; current = widthRef.value },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            current =
                                (current + dragAmount.x.toDp().value).coerceIn(minWidth, maxWidth)
                            onResizingRef.value(current)
                        },
                        onDragEnd = { isResizing = false; onResizeEndRef.value(current) },
                        onDragCancel = {
                            isResizing = false; onResizeEndRef.value(widthRef.value)
                        }
                    )
                }
        )
    }
}
