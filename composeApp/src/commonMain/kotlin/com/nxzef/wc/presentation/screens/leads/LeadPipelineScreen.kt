package com.nxzef.wc.presentation.screens.leads

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.LeadFilterBar
import com.nxzef.wc.presentation.components.RefreshButton
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.shared.model.UserRole
import com.nxzef.wc.shared.util.DateUtils
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val BOARD_COL_DEFAULT = 300f

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
                                icon = Icons.AutoMirrored.Filled.ViewList,
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
                            (lead.customStatus?.id?.let { state.filterStatusIds.contains(it) }
                                ?: false)
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
                                        viewModel.onAction(
                                            LeadPipelineAction.OnColumnWidthChange(key, width)
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    onLeadClick = { lead ->
                                        viewModel.onAction(LeadPipelineAction.SelectLead(lead))
                                    }
                                )

                                PipelineViewLayout.BOARD -> {
                                    val colWidth =
                                        if (isCompact) (screenWidth * 0.85f).value
                                        else BOARD_COL_DEFAULT

                                    KanbanBoard(
                                        statuses = state.statuses,
                                        leads = filteredLeads,
                                        taskCounts = state.taskCounts,
                                        filterStatusIds = state.filterStatusIds,
                                        isCompact = isCompact,
                                        columnWidth = colWidth.dp,
                                        horizontalPadding = horizontalPadding,
                                        syncingLeadIds = state.syncingLeadIds,
                                        onAddStatus = {
                                            viewModel.onAction(
                                                LeadPipelineAction.ShowCreateStatusDialog
                                            )
                                        },
                                        onDeleteStatus = { status ->
                                            viewModel.onAction(
                                                LeadPipelineAction.RequestDeleteStatus(status)
                                            )
                                        },
                                        onLeadClick = { lead ->
                                            viewModel.onAction(LeadPipelineAction.SelectLead(lead))
                                        },
                                        onStatusChange = { leadId, statusId ->
                                            viewModel.onAction(
                                                LeadPipelineAction.UpdateStatus(leadId, statusId)
                                            )
                                        },
                                        modifier = Modifier.fillMaxSize()
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
