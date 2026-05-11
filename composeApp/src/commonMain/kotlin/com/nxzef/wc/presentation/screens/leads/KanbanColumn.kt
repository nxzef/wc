package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.toComposeColor
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus

@Composable
fun KanbanColumn(
    status: LeadStatus,
    leads: List<Lead>,
    taskCounts: Map<String, Int>,
    draggingLeadId: String?,
    isHighlighted: Boolean,
    onLeadClick: (Lead) -> Unit,
    onDeleteStatus: (() -> Unit)?,
    onDragStart: (lead: Lead, taskCount: Int, windowPos: Offset, size: IntSize) -> Unit,
    onDrag: (leadId: String, delta: Offset) -> Unit,
    onDragEnd: (leadId: String) -> Unit,
    onDragCancel: (leadId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = status.color.toComposeColor()

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
        // ── Column header ─────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, start = 4.dp, end = 4.dp)
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
            if (onDeleteStatus != null) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDeleteStatus, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete status",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ── Lead cards ───────────────────────────────────────────────────────
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
}
