package com.nxzef.wc.presentation.screens.leads

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.LeadFilterBar
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.presentation.components.RefreshButton
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.components.toComposeColor
import com.nxzef.wc.shared.util.DateUtils
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.UserRole
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val StarGold = Color(0xFFFFC107)

val STATUS_COLORS = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#E91E63",
    "#9C27B0", "#009688", "#FF5722", "#607D8B"
)

private const val BOARD_COL_DEFAULT = 300f

private const val COL_NAME     = "list_name"
private const val COL_STATUS   = "list_status"
private const val COL_DATE     = "list_date"
private const val COL_PHONE    = "list_phone"
private const val COL_EVENT    = "list_event"
private const val COL_SOURCE   = "list_source"
private const val COL_PRIORITY = "list_priority"

private val LIST_COL_DEFAULTS = mapOf(
    COL_NAME     to 200f,
    COL_STATUS   to 130f,
    COL_DATE     to 110f,
    COL_PHONE    to 140f,
    COL_EVENT    to 100f,
    COL_SOURCE   to 110f,
    COL_PRIORITY to 90f
)

@Composable
private fun SegmentTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(15.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeadPipelineScreen(
    onBack: () -> Unit,
    onAddLead: () -> Unit,
    onViewQuotes: (leadId: String, clientName: String, clientEmail: String) -> Unit,
    onViewBooking: () -> Unit,
    viewModel: LeadPipelineViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draggingLeadId by remember { mutableStateOf<String?>(null) }
    val snackbarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isMainScreen = remember { SessionManager.getRole() == UserRole.LEAD_MANAGER }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LeadPipelineUiEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is LeadPipelineUiEvent.ShowError -> snackbarState.showSnackbar(event.message)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isCompact = screenWidth < 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarState) },
            topBar = {
                WCTopBar(
                    title = if (isCompact) "Pipeline" else "Lead Pipeline",
                    subtitle = "${state.leads.size} leads",
                    onBack = if (isMainScreen) null else onBack,
                    showNotificationIcon = isMainScreen,
                    actions = {
                        val isBoardActive = state.viewLayout == PipelineViewLayout.BOARD
                        Row(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.shapes.extraLarge
                                )
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SegmentTab(
                                icon = Icons.Default.ViewColumn,
                                label = "Board",
                                selected = isBoardActive,
                                onClick = {
                                    if (!isBoardActive)
                                        viewModel.onAction(LeadPipelineAction.ToggleViewLayout)
                                }
                            )
                            SegmentTab(
                                icon = Icons.Default.ViewList,
                                label = "List",
                                selected = !isBoardActive,
                                onClick = {
                                    if (isBoardActive)
                                        viewModel.onAction(LeadPipelineAction.ToggleViewLayout)
                                }
                            )
                        }

                        RefreshButton(
                            isLoading = state.isLoading || state.isRefreshing,
                            onClick = { RefreshManager.triggerRefresh() }
                        )
                        Button(
                            onClick = onAddLead,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(end = if (isCompact) 8.dp else 16.dp),
                            contentPadding = if (isCompact)
                                PaddingValues(horizontal = 12.dp)
                            else ButtonDefaults.ContentPadding
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            if (!isCompact) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Lead")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)

            // Filtered leads (shared between both layouts)
            val filteredLeads = remember(
                state.leads,
                state.searchQuery,
                state.filterPriority,
                state.filterSource,
                state.filterEventType,
                state.filterDateMonth,
                state.filterDateYear,
                state.filterStatusIds
            ) {
                state.leads.filter { lead ->
                    val matchesQuery = state.searchQuery.isBlank() ||
                        lead.fullName.contains(state.searchQuery, ignoreCase = true) ||
                        lead.phone.contains(state.searchQuery) ||
                        (lead.email?.contains(state.searchQuery, ignoreCase = true) == true) ||
                        lead.eventType.name.contains(state.searchQuery, ignoreCase = true) ||
                        lead.source.name.contains(state.searchQuery, ignoreCase = true)

                    val matchesPriority =
                        state.filterPriority == null || lead.priority == state.filterPriority
                    val matchesSource =
                        state.filterSource == null || lead.source == state.filterSource
                    val matchesEventType =
                        state.filterEventType == null || lead.eventType == state.filterEventType
                    val matchesStatus = state.filterStatusIds.isEmpty() ||
                        (lead.customStatus?.id?.let { state.filterStatusIds.contains(it) } ?: false)
                    val matchesMonth = state.filterDateMonth == null ||
                        DateUtils.getMonth(lead.eventDate) == state.filterDateMonth
                    val matchesYear = state.filterDateYear == null ||
                        DateUtils.getYear(lead.eventDate) == state.filterDateYear

                    matchesQuery && matchesPriority && matchesSource && matchesEventType &&
                        matchesMonth && matchesYear && matchesStatus
                }
            }

            when {
                state.isLoading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onAction(LeadPipelineAction.LoadLeads) }) {
                        Text("Retry")
                    }
                }
                state.statuses.isEmpty() -> Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No statuses yet. Create your first status.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onAction(LeadPipelineAction.ShowCreateStatusDialog) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Status")
                    }
                }
                else -> {
                    val horizontalPadding = if (isCompact) 16.dp else 24.dp

                    Column(modifier = contentModifier) {
                        LeadFilterBar(
                            state = state,
                            onAction = viewModel::onAction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 8.dp)
                        )

                        Crossfade(
                            targetState = state.viewLayout,
                            animationSpec = tween(durationMillis = 250),
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) { layout ->
                            when (layout) {
                                PipelineViewLayout.LIST -> LeadListView(
                                    leads = filteredLeads,
                                    columnWidths = state.columnWidths,
                                    onColumnWidthChange = { key, width ->
                                        viewModel.onAction(LeadPipelineAction.OnColumnWidthChange(key, width))
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    onLeadClick = { lead ->
                                        viewModel.onAction(LeadPipelineAction.SelectLead(lead))
                                    }
                                )
                                PipelineViewLayout.BOARD -> {
                                    var columnBounds by remember {
                                        mutableStateOf(mapOf<String, androidx.compose.ui.layout.LayoutCoordinates>())
                                    }
                                    var hoveredStatusId by remember { mutableStateOf<String?>(null) }

                                    LaunchedEffect(state.filterStatusIds) {
                                        columnBounds = columnBounds.filter { it.value.isAttached }
                                    }

                                    val defaultColWidth =
                                        if (isCompact) (screenWidth * 0.85f).value else BOARD_COL_DEFAULT

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            if (isCompact) 16.dp else 24.dp
                                        )
                                    ) {
                                        Spacer(modifier = Modifier.width(horizontalPadding / 2))

                                        state.statuses
                                            .filter { status ->
                                                state.filterStatusIds.isEmpty() ||
                                                    state.filterStatusIds.contains(status.id)
                                            }
                                            .forEach { status ->
                                                key(status.id) {
                                                    val isDraggingFromThisColumn = state.leads.any {
                                                        it.id == draggingLeadId &&
                                                            it.customStatus?.id == status.id
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .width(defaultColWidth.dp)
                                                            .fillMaxHeight()
                                                            .zIndex(if (isDraggingFromThisColumn) 100f else 0f)
                                                    ) {
                                                        KanBanColumn(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .onGloballyPositioned { coords ->
                                                                    columnBounds = columnBounds + (status.id to coords)
                                                                },
                                                            status = status,
                                                            leads = filteredLeads.filter {
                                                                it.customStatus?.id == status.id
                                                            },
                                                            taskCounts = state.taskCounts,
                                                            onLeadClick = { lead ->
                                                                viewModel.onAction(
                                                                    LeadPipelineAction.SelectLead(lead)
                                                                )
                                                            },
                                                            onDeleteStatus = if (status.isDefault) null else {
                                                                {
                                                                    viewModel.onAction(
                                                                        LeadPipelineAction.RequestDeleteStatus(status)
                                                                    )
                                                                }
                                                            },
                                                            isHighlighted = hoveredStatusId == status.id,
                                                            onDragStart = { draggingLeadId = it },
                                                            onDrag = { _, currentPosition ->
                                                                var found = false
                                                                columnBounds.forEach { (sid, coords) ->
                                                                    if (coords.isAttached &&
                                                                        coords.boundsInWindow().contains(currentPosition)
                                                                    ) {
                                                                        hoveredStatusId = sid
                                                                        found = true
                                                                    }
                                                                }
                                                                if (!found) hoveredStatusId = null
                                                            },
                                                            onDragEnd = { leadId, finalPosition ->
                                                                draggingLeadId = null
                                                                hoveredStatusId = null
                                                                columnBounds.forEach { (sid, coords) ->
                                                                    if (coords.isAttached &&
                                                                        coords.boundsInWindow().contains(finalPosition)
                                                                    ) {
                                                                        viewModel.onAction(
                                                                            LeadPipelineAction.UpdateStatus(leadId, sid)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.onAction(LeadPipelineAction.ShowCreateStatusDialog)
                                            },
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier.width(160.dp).padding(top = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add Status")
                                        }

                                        Spacer(modifier = Modifier.width(horizontalPadding / 2))
                                    }
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
            newTaskTitle = state.newTaskTitle,
            onTaskTitleChange = { viewModel.onAction(LeadPipelineAction.OnNewTaskTitleChange(it)) },
            onAddTask = { viewModel.onAction(LeadPipelineAction.OnAddTask) },
            availableStatuses = state.statuses,
            onDismiss = { viewModel.onAction(LeadPipelineAction.DismissDetail) },
            onUpdateStatus = { customStatusId, notes ->
                viewModel.onAction(LeadPipelineAction.UpdateStatus(lead.id, customStatusId, notes))
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onViewQuotes = {
                onViewQuotes(lead.id, lead.fullName, lead.email ?: "")
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onViewBooking = {
                onViewBooking()
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onTaskToggle = { taskId, done ->
                viewModel.onAction(LeadPipelineAction.MarkTaskDone(taskId, done))
            },
            onDeleteTask = { taskId ->
                viewModel.onAction(LeadPipelineAction.OnDeleteTask(taskId))
            }
        )
    }

    if (state.showCreateStatusDialog) {
        CreateStatusDialog(
            existingStatuses = state.statuses,
            onDismiss = { viewModel.onAction(LeadPipelineAction.HideCreateStatusDialog) },
            onConfirm = { name, color ->
                viewModel.onAction(LeadPipelineAction.CreateStatus(name, color))
            },
            onValidationError = { msg ->
                coroutineScope.launch { snackbarState.showSnackbar(msg) }
            }
        )
    }

    state.statusToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.onAction(LeadPipelineAction.DismissDeleteStatusDialog) },
            title = { Text("Delete status?") },
            text = {
                Text("Delete '${target.name}'? Leads in this status will be moved to the default status.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onAction(LeadPipelineAction.ConfirmDeleteStatus) }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onAction(LeadPipelineAction.DismissDeleteStatusDialog)
                }) { Text("Cancel") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List View
// ─────────────────────────────────────────────────────────────────────────────

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
                    COL_NAME     to "Name",
                    COL_STATUS   to "Status",
                    COL_DATE     to "Event Date",
                    COL_PHONE    to "Phone",
                    COL_EVENT    to "Event",
                    COL_SOURCE   to "Source",
                    COL_PRIORITY to "Priority"
                ).forEach { (key, label) ->
                    val currentWidth = resizingWidths[key] ?: columnWidths[key] ?: LIST_COL_DEFAULTS.getValue(key)
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
                            LeadStatusBadge(statusName = lead.statusName, color = lead.customStatus?.color)
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
                                        Icon(Icons.Default.Star, null, Modifier.size(13.dp), tint = StarGold)
                                    }
                                }
                            } else {
                                Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (index < leads.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isResizing) 1f else 0.55f),
                    MaterialTheme.shapes.extraSmall
                )
                .pointerInput(Unit) {
                    var current = widthRef.value
                    detectDragGestures(
                        onDragStart = { isResizing = true; current = widthRef.value },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            current = (current + dragAmount.x.toDp().value).coerceIn(minWidth, maxWidth)
                            onResizingRef.value(current)
                        },
                        onDragEnd = { isResizing = false; onResizeEndRef.value(current) },
                        onDragCancel = { isResizing = false; onResizeEndRef.value(widthRef.value) }
                    )
                }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kanban Column
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KanBanColumn(
    modifier: Modifier = Modifier,
    status: LeadStatus,
    leads: List<Lead>,
    taskCounts: Map<String, Int> = emptyMap(),
    onLeadClick: (Lead) -> Unit,
    onDeleteStatus: (() -> Unit)? = null,
    isHighlighted: Boolean = false,
    onDragStart: (String) -> Unit,
    onDrag: (String, Offset) -> Unit,
    onDragEnd: (String, Offset) -> Unit
) {
    val color = status.color.toComposeColor()

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
                if (isHighlighted) Modifier.border(2.dp, color, MaterialTheme.shapes.medium)
                else Modifier
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, start = 4.dp, end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, MaterialTheme.shapes.extraSmall)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = status.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
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
                Spacer(modifier = Modifier.width(4.dp))
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().graphicsLayer { clip = false }
        ) {
            items(leads, key = { it.id }) { lead ->
                DraggableLeadCard(
                    lead = lead,
                    taskCount = taskCounts[lead.id] ?: 0,
                    onClick = { onLeadClick(lead) },
                    onDragStart = { onDragStart(lead.id) },
                    onDrag = { currentPosition -> onDrag(lead.id, currentPosition) },
                    onDragEnd = { finalPosition -> onDragEnd(lead.id, finalPosition) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CreateStatusDialog(
    existingStatuses: List<LeadStatus>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String) -> Unit,
    onValidationError: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(STATUS_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("New Status", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Status Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Text("Pick a Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    STATUS_COLORS.forEach { hex ->
                        val c = hex.toComposeColor()
                        Box(
                            modifier = Modifier
                                .size(if (selectedColor == hex) 32.dp else 28.dp)
                                .background(c, MaterialTheme.shapes.small)
                                .then(
                                    if (selectedColor == hex)
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            MaterialTheme.shapes.small
                                        )
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.isEmpty() -> onValidationError("Status name is required")
                        existingStatuses.any { it.name.equals(trimmed, ignoreCase = true) } ->
                            onValidationError("A status with this name already exists")
                        else -> onConfirm(trimmed, selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) { Text("Cancel") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Draggable lead card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DraggableLeadCard(
    lead: Lead,
    taskCount: Int = 0,
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
                if (isDragging) { clip = false; scaleX = 1.02f; scaleY = 1.02f }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true; onDragStart() },
                    onDragEnd = {
                        val centerPos = globalPosition + offset +
                            Offset(cardSize.width / 2f, cardSize.height / 2f)
                        isDragging = false; offset = Offset.Zero; onDragEnd(centerPos)
                    },
                    onDragCancel = { isDragging = false; offset = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                        val centerPos = globalPosition + offset +
                            Offset(cardSize.width / 2f, cardSize.height / 2f)
                        onDrag(centerPos)
                    }
                )
            }
    ) {
        LeadCard(lead = lead, taskCount = taskCount, onClick = onClick, elevation = elevation)
    }
}

@Composable
fun LeadCard(
    lead: Lead,
    taskCount: Int = 0,
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
                if (lead.priority > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(lead.priority) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = StarGold
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

// ─────────────────────────────────────────────────────────────────────────────
// Lead detail dialog
// ─────────────────────────────────────────────────────────────────────────────

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
                LeadStatusBadge(statusName = lead.statusName, color = lead.customStatus?.color)
            }
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
                                            tint = StarGold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Button(
                        onClick = onViewQuotes,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View Quotes & Financials")
                    }

                    Button(
                        onClick = { onViewBooking() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View Booking")
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

                    if (availableStatuses.isNotEmpty()) {
                        Text(
                            text = "Update Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableStatuses.forEach { status ->
                                val isCurrentStatus = status.id == lead.customStatus?.id
                                val statusColor = status.color.toComposeColor()
                                OutlinedButton(
                                    onClick = { onUpdateStatus(status.id, notes.ifBlank { null }) },
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
                }

                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "My Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            isTasksLoading -> CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                            tasks.isEmpty() -> Text(
                                "No tasks yet",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) { Text("Close") }
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
