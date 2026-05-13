package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.toComposeColor
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task

@Composable
fun LeadDetailDialog(
    modifier: Modifier = Modifier,
    lead: Lead,
    tasks: List<Task>,
    isTasksLoading: Boolean,
    newTaskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    availableStatuses: List<LeadStatus>,
    onDismiss: () -> Unit,
    onUpdateStatus: (customStatusId: String, notes: String?) -> Unit,
    onViewQuotes: () -> Unit,
    onViewBooking: () -> Unit,
    onTaskToggle: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit,
    onMarkWon: () -> Unit = {},
    onMarkLost: () -> Unit = {},
    onEditLead: () -> Unit = {}
) {
    var notes by remember { mutableStateOf(lead.notes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.88f)
                .widthIn(min = 680.dp, max = 1100.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = lead.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${lead.eventType.name} — ${lead.eventDate ?: "Date TBD"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeadStatusBadge(statusName = lead.statusName, color = lead.customStatus?.color)
                        IconButton(onClick = { onEditLead(); onDismiss() }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Lead",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DialogSectionLabel("Lead Information")

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DetailRow("📱 Phone", lead.phone)
                            lead.email?.let { DetailRow("📧 Email", it) }
                            DetailRow("🎉 Event", lead.eventType.name)
                            lead.eventDate?.let { DetailRow("📅 Date", it) }
                            lead.location?.let { DetailRow("📍 Location", it) }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📣 Source",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(90.dp)
                                )
                                LeadSourceBadge(source = lead.source)
                            }
                            if (lead.priority > 0) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⭐ Priority",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        repeat(lead.priority) {
                                            Icon(
                                                Icons.Default.Star, null,
                                                modifier = Modifier.size(16.dp),
                                                tint = WCTheme.colors.starGold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (availableStatuses.isNotEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            DialogSectionLabel("Update Status")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableStatuses.forEach { status ->
                                    val isCurrentStatus = status.id == lead.customStatus?.id
                                    val statusColor = status.color.toComposeColor()
                                    OutlinedButton(
                                        onClick = {
                                            onUpdateStatus(status.id, notes.ifBlank { null })
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = statusColor,
                                            containerColor = if (isCurrentStatus)
                                                statusColor.copy(alpha = 0.1f) else Color.Transparent
                                        ),
                                        border = BorderStroke(
                                            if (isCurrentStatus) 2.dp else 1.dp,
                                            if (isCurrentStatus) statusColor
                                            else MaterialTheme.colorScheme.outline
                                        ),
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = status.name,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        DialogSectionLabel("Notes")
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Lead Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            minLines = 3
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onDismiss(); onViewQuotes() },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Quotes & Financials",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Button(
                                onClick = { onDismiss(); onViewBooking() },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("View Booking", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DialogSectionLabel("My Tasks")

                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                isTasksLoading -> CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                tasks.isEmpty() -> Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                    Text(
                                        "No tasks yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                }

                                else -> LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(tasks) { task ->
                                        TaskCheckItem(
                                            task = task,
                                            onToggle = { onTaskToggle(task.id, it) },
                                            onDelete = { onDeleteTask(task.id) }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = onTaskTitleChange,
                                placeholder = {
                                    Text("New task…", style = MaterialTheme.typography.bodySmall)
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true
                            )
                            Button(
                                onClick = onAddTask,
                                enabled = newTaskTitle.isNotBlank(),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) { Text("Add") }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!lead.isWon) {
                            OutlinedButton(
                                onClick = { onMarkWon(); onDismiss() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4CAF50)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Mark Won ✓", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (!lead.isLost) {
                            OutlinedButton(
                                onClick = { onMarkLost(); onDismiss() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Mark Lost ✗", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DialogSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
