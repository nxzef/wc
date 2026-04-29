package com.nxzef.wc.presentation.screens.leads

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.AddTaskDialog
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

val PIPELINE_STAGES = listOf(LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.NEGOTIATING)

@Composable
fun LeadPipelineScreen(
    onBack: () -> Unit,
    onAddLead: () -> Unit,
    onViewQuotes: (String) -> Unit,
    onViewBooking: () -> Unit,
    viewModel: LeadPipelineViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draggingLeadId by remember { mutableStateOf<String?>(null) }

    val activeCount = state.leads.count { it.status in PIPELINE_STAGES }
    val wonCount = state.leads.count { it.status == LeadStatus.WON }
    val lostCount = state.leads.count { it.status == LeadStatus.LOST }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp

        Scaffold(
            topBar = {
                WCTopBar(
                    title = if (isCompact) "Pipeline" else "Lead Pipeline",
                    subtitle = "${state.leads.size} leads",
                    onBack = onBack,
                    actions = {
                        Button(
                            onClick = onAddLead,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(end = if (isCompact) 8.dp else 16.dp),
                            contentPadding = if (isCompact) PaddingValues(horizontal = 12.dp) else ButtonDefaults.ContentPadding
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            if (!isCompact) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Lead")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = state.currentFilter == PipelineFilter.ACTIVE,
                        onClick = { viewModel.onAction(LeadPipelineAction.SetFilter(PipelineFilter.ACTIVE)) },
                        label = { Text("Active ($activeCount)") }
                    )
                    FilterChip(
                        selected = state.currentFilter == PipelineFilter.WON,
                        onClick = { viewModel.onAction(LeadPipelineAction.SetFilter(PipelineFilter.WON)) },
                        label = { Text("Won ($wonCount)") }
                    )
                    FilterChip(
                        selected = state.currentFilter == PipelineFilter.LOST,
                        onClick = { viewModel.onAction(LeadPipelineAction.SetFilter(PipelineFilter.LOST)) },
                        label = { Text("Lost ($lostCount)") }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
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

                        state.currentFilter == PipelineFilter.ACTIVE -> {
                            var columnBounds by remember {
                                mutableStateOf(
                                    mapOf<LeadStatus, androidx.compose.ui.layout.LayoutCoordinates>()
                                )
                            }
                            var hoveredStage by remember { mutableStateOf<LeadStatus?>(null) }

                            val horizontalPadding = if (isCompact) 16.dp else 24.dp
                            val columnWidth = if (isCompact) screenWidth * 0.85f else 320.dp

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 24.dp)
                            ) {
                                Spacer(modifier = Modifier.width(horizontalPadding - (if (isCompact) 16.dp else 24.dp)))

                                Spacer(modifier = Modifier.width(horizontalPadding / 2))

                                PIPELINE_STAGES.forEach { stage ->
                                    val isDraggingFromThisColumn =
                                        state.leads.any { it.id == draggingLeadId && it.status == stage }

                                    KanBanColumn(
                                        modifier = Modifier
                                            .width(columnWidth)
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
                                Spacer(modifier = Modifier.width(horizontalPadding / 2))
                            }
                        }

                        else -> {
                            val filterStatus = when (state.currentFilter) {
                                PipelineFilter.WON -> LeadStatus.WON
                                PipelineFilter.LOST -> LeadStatus.LOST
                                else -> LeadStatus.NEW
                            }
                            val filteredLeads = state.leads.filter { it.status == filterStatus }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(filteredLeads) { lead ->
                                    LeadCard(
                                        lead = lead,
                                        onClick = { viewModel.onAction(LeadPipelineAction.SelectLead(lead)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    state.selectedLead?.let { lead ->
            LeadDetailDialog(
                modifier = Modifier.widthIn(max = 1000.dp),
                lead = lead,
                tasks = state.tasks,
                isTasksLoading = state.isTasksLoading,
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
                },
                onViewBooking = {
                    onViewBooking()
                    viewModel.onAction(LeadPipelineAction.DismissDetail)
                },
                onTaskToggle = { taskId, done ->
                    viewModel.onAction(LeadPipelineAction.MarkTaskDone(taskId, done))
                },
                onAddTaskClick = {
                    viewModel.onAction(LeadPipelineAction.ShowAddTaskDialog)
                },
                onDeleteTask = { taskId ->
                    viewModel.onAction(LeadPipelineAction.OnDeleteTask(taskId))
                }
            )
    }

    if (state.showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { viewModel.onAction(LeadPipelineAction.HideAddTaskDialog) },
            onConfirm = { 
                viewModel.onAction(LeadPipelineAction.OnNewTaskTitleChange(it))
                viewModel.onAction(LeadPipelineAction.OnAddTask) 
            }
        )
    }
}

@Composable
fun KanBanColumn(
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
    modifier: Modifier = Modifier,
    lead: Lead,
    tasks: List<Task>,
    isTasksLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdateStatus: (LeadStatus, String?) -> Unit,
    onViewQuotes: () -> Unit,
    onViewBooking: () -> Unit,
    onTaskToggle: (String, Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var notes by remember { mutableStateOf(lead.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(max = 800.dp),
        shape = MaterialTheme.shapes.large,
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = lead.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lead.eventType} — ${lead.eventDate ?: "Date TBD"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LeadStatusBadge(status = lead.status)
            }
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Side: Details & Status
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Lead Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    if (lead.status == LeadStatus.NEGOTIATING) {
                        Button(
                            onClick = onViewQuotes,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Quotes & Financials")
                        }
                    }

                    if (lead.status == LeadStatus.WON) {
                        Button(
                            onClick = { onViewBooking() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WCTheme.colors.statusWon
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Booking")
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Lead Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        minLines = 3
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    if (lead.status == LeadStatus.LOST) {
                        Button(
                            onClick = { onUpdateStatus(LeadStatus.NEW, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Reopen Lead")
                        }
                    } else if (lead.status != LeadStatus.WON) {
                        Text(
                            text = "Update Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            val availableStatuses = when (lead.status) {
                                LeadStatus.NEW -> listOf(LeadStatus.CONTACTED, LeadStatus.LOST)
                                LeadStatus.CONTACTED -> listOf(LeadStatus.NEGOTIATING, LeadStatus.LOST)
                                LeadStatus.NEGOTIATING -> listOf(LeadStatus.WON, LeadStatus.LOST)
                                else -> emptyList()
                            }

                            availableStatuses.forEach { stage ->
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
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text(text = stage.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Right Side: Tasks
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Follow-up Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onAddTaskClick) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "Add Task",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (isTasksLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else if (tasks.isEmpty()) {
                            Text(
                                "No tasks for this lead",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
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
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.medium
            ) {
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
