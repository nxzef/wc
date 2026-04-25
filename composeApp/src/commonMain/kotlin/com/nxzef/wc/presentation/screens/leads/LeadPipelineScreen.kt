package com.nxzef.wc.presentation.screens.leads

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

val PIPELINE_STAGES = LeadStatus.entries

@Composable
fun LeadPipelineScreen(
    onBack: () -> Unit,
    onAddLead: () -> Unit,
    onViewQuotes: (String) -> Unit,
    viewModel: LeadPipelineViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draggingLeadId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            WCTopBar(
                title = "Lead Pipeline",
                subtitle = "${state.leads.size} total leads",
                onBack = onBack,
                actions = {
                    Button(
                        onClick = onAddLead,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Lead")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .graphicsLayer { clip = false }
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.onAction(LeadPipelineAction.LoadLeads)
                    }) { Text("Retry") }
                }

                else -> {
                    var columnBounds by remember { mutableStateOf(mapOf<LeadStatus, androidx.compose.ui.layout.LayoutCoordinates>()) }
                    var hoveredStage by remember { mutableStateOf<LeadStatus?>(null) }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .graphicsLayer {
                                clip = false
                                translationX = 0f
                            }
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        PIPELINE_STAGES.forEach { stage ->
                            val isDraggingFromThisColumn =
                                state.leads.any { it.id == draggingLeadId && it.status == stage }

                            KanbanColumn(
                                modifier = Modifier
                                    .width(320.dp)
                                    .zIndex(if (isDraggingFromThisColumn) 100f else 0f)
                                    .onGloballyPositioned { coords ->
                                        columnBounds = columnBounds + (stage to coords)
                                    },
                                stage = stage,
                                leads = state.leads.filter {
                                    it.status == stage
                                },
                                onLeadClick = { lead ->
                                    viewModel.onAction(
                                        LeadPipelineAction.SelectLead(lead)
                                    )
                                },
                                isHighlighted = hoveredStage == stage,
                                onDragStart = { draggingLeadId = it },
                                onDrag = { _, currentPosition ->
                                    var found = false
                                    columnBounds.forEach { (s, coords) ->
                                        if (coords.isAttached && coords.boundsInWindow()
                                                .contains(currentPosition)
                                        ) {
                                            hoveredStage = s
                                            found = true
                                        }
                                    }
                                    if (!found) hoveredStage = null
                                },
                                onDragEnd = { leadId, finalPosition ->
                                    draggingLeadId = null
                                    hoveredStage = null
                                    columnBounds.forEach { (columnStage, coords) ->
                                        if (coords.isAttached) {
                                            val rect = coords.boundsInWindow()
                                            if (rect.contains(finalPosition)) {
                                                viewModel.onAction(
                                                    LeadPipelineAction.UpdateStatus(
                                                        leadId,
                                                        columnStage
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    state.selectedLead?.let { lead ->
        LeadDetailDialog(
            lead = lead,
            onDismiss = {
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onUpdateStatus = { status, notes ->
                viewModel.onAction(
                    LeadPipelineAction.UpdateStatus(lead.id, status, notes)
                )
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onViewQuotes = {
                onViewQuotes(lead.id)
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            }
        )
    }
}

@Composable
fun KanbanColumn(
    modifier: Modifier = Modifier,
    stage: LeadStatus,
    leads: List<Lead>,
    onLeadClick: (Lead) -> Unit,
    isHighlighted: Boolean = false,
    onDragStart: (String) -> Unit,
    onDrag: (String, Offset) -> Unit,
    onDragEnd: (String, Offset) -> Unit
) {
    val color = when (stage) {
        LeadStatus.NEW -> WCTheme.colors.statusNew
        LeadStatus.CONTACTED -> WCTheme.colors.statusContacted
        LeadStatus.NEGOTIATING -> WCTheme.colors.statusNegotiating
        LeadStatus.WON -> WCTheme.colors.statusWon
        LeadStatus.LOST -> WCTheme.colors.statusLost
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer { clip = false }
            .background(
                if (isHighlighted) color.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.medium
            )
            .then(
                if (isHighlighted) Modifier.border(
                    2.dp, color, MaterialTheme.shapes.medium
                ) else Modifier
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, start = 4.dp, end = 4.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = color,
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stage.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    text = leads.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false }
        ) {
            items(leads, key = { it.id }) { lead ->
                DraggableLeadCard(
                    lead = lead,
                    onClick = { onLeadClick(lead) },
                    onDragStart = { onDragStart(lead.id) },
                    onDrag = { currentPosition -> onDrag(lead.id, currentPosition) },
                    onDragEnd = { finalPosition -> onDragEnd(lead.id, finalPosition) }
                )
            }
        }
    }
}

@Composable
fun DraggableLeadCard(
    lead: Lead,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var globalPosition by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                if (!isDragging) {
                    globalPosition = coords.positionInWindow()
                    cardSize = coords.size
                }
            }
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .zIndex(if (isDragging) 1000f else 0f)
            .graphicsLayer {
                if (isDragging) {
                    clip = false
                    scaleX = 1.02f
                    scaleY = 1.02f
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        val centerPos = globalPosition + offset + Offset(
                            cardSize.width / 2f,
                            cardSize.height / 2f
                        )
                        isDragging = false
                        offset = Offset.Zero
                        onDragEnd(centerPos)
                    },
                    onDragCancel = {
                        isDragging = false
                        offset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                        val centerPos = globalPosition + offset + Offset(
                            cardSize.width / 2f,
                            cardSize.height / 2f
                        )
                        onDrag(centerPos)
                    }
                )
            }
    ) {
        LeadCard(
            lead = lead,
            onClick = onClick,
            elevation = elevation
        )
    }
}

@Composable
fun LeadCard(
    lead: Lead,
    onClick: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp = 0.dp
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (elevation == 0.dp) BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ) else null
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
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = lead.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LeadInfoRow(Icons.Default.CalendarMonth, lead.eventDate ?: "Date not set")
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

@Composable
fun LeadDetailDialog(
    lead: Lead,
    onDismiss: () -> Unit,
    onUpdateStatus: (LeadStatus, String?) -> Unit,
    onViewQuotes: () -> Unit
) {
    var notes by remember { mutableStateOf(lead.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lead.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                LeadStatusBadge(status = lead.status)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.widthIn(max = 400.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("📱 Phone", lead.phone)
                    lead.email?.let { DetailRow("📧 Email", it) }
                    DetailRow("🎉 Event", lead.eventType.name)
                    lead.eventDate?.let { DetailRow("📅 Date", it) }
                    lead.location?.let { DetailRow("📍 Location", it) }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "📣 Source",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp)
                        )
                        LeadSourceBadge(source = lead.source)
                    }
                }

                HorizontalDivider()

                Button(
                    onClick = onViewQuotes,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View Quotes & Financials")
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Lead Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    minLines = 3
                )

                Text(
                    text = "Quick Status Update",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    PIPELINE_STAGES.forEach { stage ->
                        if (stage != lead.status) {
                            val stageColor = when (stage) {
                                LeadStatus.NEW -> WCTheme.colors.statusNew
                                LeadStatus.CONTACTED -> WCTheme.colors.statusContacted
                                LeadStatus.NEGOTIATING -> WCTheme.colors.statusNegotiating
                                LeadStatus.WON -> WCTheme.colors.statusWon
                                LeadStatus.LOST -> WCTheme.colors.statusLost
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(stage, notes.ifBlank { null }) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = stageColor),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(text = stage.name, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
