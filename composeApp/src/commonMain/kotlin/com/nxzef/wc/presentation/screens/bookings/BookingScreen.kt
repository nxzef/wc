package com.nxzef.wc.presentation.screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.AddTaskDialog
import com.nxzef.wc.presentation.components.RefreshButton
import com.nxzef.wc.presentation.components.WCSearchBar
import com.nxzef.wc.shared.util.DateUtils
import com.nxzef.wc.util.RefreshManager
import com.nxzef.wc.presentation.components.BookingStatusBadge
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.screens.leads.WCDropdown
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onBack: () -> Unit,
    onExpenses: (bookingId: String) -> Unit = {},
    viewModel: BookingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is BookingUiEvent.ShowSnackbar ->
                    snackbarState.showSnackbar(event.message)

                BookingUiEvent.BookingCreated ->
                    snackbarState.showSnackbar("Booking created!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Bookings",
                subtitle = "${state.bookings.size} total bookings",
                onBack = onBack,
                actions = {
                    RefreshButton(
                        isLoading = state.isLoading || state.isRefreshing,
                        onClick = { RefreshManager.triggerRefresh() }
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
            ) {
                // ── Search bar ──────────────────────────────────────────────
                WCSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onAction(BookingAction.OnSearchQueryChange(it)) },
                    placeholder = "Search bookings…",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )

                // ── Status chips (centered) ─────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = state.filterStatus == null,
                            onClick = { viewModel.onAction(BookingAction.OnFilterStatus(null)) },
                            label = { Text("All") },
                            shape = MaterialTheme.shapes.large
                        )
                        BookingStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.filterStatus == status,
                                onClick = { viewModel.onAction(BookingAction.OnFilterStatus(status)) },
                                label = { Text(status.name.replace("_", " ")) },
                                shape = MaterialTheme.shapes.large
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ── Content ─────────────────────────────────────────────────
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        viewModel.filteredBookings.isEmpty() -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No bookings found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(viewModel.filteredBookings) { booking ->
                                    val leadName =
                                        state.leads.find { it.id == booking.leadId }?.fullName
                                            ?: "Unknown Lead"
                                    BookingCard(
                                        leadName = leadName,
                                        booking = booking,
                                        onClick = {
                                            viewModel.onAction(
                                                BookingAction.SelectBooking(booking)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Booking detail dialog
    state.selectedBooking?.let { booking ->
        BookingDetailDialog(
            modifier = Modifier.widthIn(max = 1000.dp),
            booking = booking,
            leads = state.leads,
            tasks = state.tasks,
            team = state.team,
            isTasksLoading = state.isTasksLoading,
            onDismiss = {
                viewModel.onAction(BookingAction.DismissDetail)
            },
            onUpdateStatus = { status ->
                viewModel.onAction(
                    BookingAction.OnUpdateStatus(booking.id, status)
                )
                viewModel.onAction(BookingAction.DismissDetail)
            },
            onAssignPhotographer = { userId ->
                viewModel.onAction(BookingAction.AssignPhotographer(booking.id, userId))
            },
            onAssignEditor = { userId ->
                viewModel.onAction(BookingAction.AssignEditor(booking.id, userId))
            },
            onTaskToggle = { taskId, isDone ->
                viewModel.onAction(BookingAction.OnTaskToggle(taskId, isDone))
            },
            onAddTaskClick = {
                viewModel.onAction(BookingAction.ShowAddTaskDialog)
            },
            onDeleteTask = { taskId ->
                viewModel.onAction(BookingAction.OnDeleteTask(taskId))
            },
            onExpenses = { onExpenses(booking.id) }
        )
    }

    if (state.showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { viewModel.onAction(BookingAction.HideAddTaskDialog) },
            onConfirm = {
                viewModel.onAction(BookingAction.OnNewTaskTitleChange(it))
                viewModel.onAction(BookingAction.OnAddTask)
            }
        )
    }
}

@Composable
fun BookingCard(
    leadName: String,
    booking: Booking,
    onClick: () -> Unit
) {
    val statusColor = when (booking.status) {
        BookingStatus.BOOKED -> WCTheme.colors.statusBooked
        BookingStatus.SHOOT_DONE -> WCTheme.colors.statusShootDone
        BookingStatus.EDITING -> WCTheme.colors.statusEditing
        BookingStatus.DELIVERED -> WCTheme.colors.statusDelivered
        BookingStatus.CLOSED -> WCTheme.colors.statusClosed
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color indicator
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp),
                shape = MaterialTheme.shapes.small,
                color = statusColor
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leadName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = booking.eventType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateUtils.formatDisplayDate(booking.eventDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            BookingStatusBadge(status = booking.status)
        }
    }
}

@Composable
fun BookingDetailDialog(
    modifier: Modifier = Modifier,
    booking: Booking,
    leads: List<com.nxzef.wc.shared.model.Lead>,
    tasks: List<com.nxzef.wc.shared.model.Task>,
    team: List<User>,
    isTasksLoading: Boolean,
    onDismiss: () -> Unit,
    onUpdateStatus: (BookingStatus) -> Unit,
    onAssignPhotographer: (String?) -> Unit,
    onAssignEditor: (String?) -> Unit,
    onTaskToggle: (String, Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onDeleteTask: (String) -> Unit,
    onExpenses: () -> Unit = {}
) {
    val lead = leads.find { it.id == booking.leadId }
    val leadName = lead?.fullName ?: "Unknown Lead"
    val photographers = team.filter { it.role == UserRole.PHOTOGRAPHER }
    val editors = team.filter { it.role == UserRole.EDITOR }

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
                // ── Header ────────────────────────────────────────────────────
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
                            text = leadName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${booking.eventType} — ${DateUtils.formatDisplayDate(booking.eventDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BookingStatusBadge(status = booking.status)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ── Body: two-panel landscape layout ─────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    // LEFT: Details + team assignments + status
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BookingDialogSectionLabel("Booking Details")

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DetailItem("📍 Location", booking.location)
                            DetailItem("📅 Date", DateUtils.formatDisplayDate(booking.eventDate))
                            DetailItem("📱 Phone", lead?.phone ?: "N/A")
                            booking.notes?.takeIf { it.isNotBlank() }?.let {
                                DetailItem("📝 Notes", it)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BookingDialogSectionLabel("Team Assignments")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WCDropdown(
                                label = "Photographer",
                                selected = team.find { it.id == booking.photographerId }?.name ?: "Unassigned",
                                options = listOf("Unassigned") + photographers.map { it.name },
                                onSelect = { name ->
                                    if (name == "Unassigned") onAssignPhotographer(null)
                                    else photographers.find { it.name == name }
                                        ?.let { onAssignPhotographer(it.id) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            WCDropdown(
                                label = "Editor",
                                selected = team.find { it.id == booking.editorId }?.name ?: "Unassigned",
                                options = listOf("Unassigned") + editors.map { it.name },
                                onSelect = { name ->
                                    if (name == "Unassigned") onAssignEditor(null)
                                    else editors.find { it.name == name }?.let { onAssignEditor(it.id) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BookingDialogSectionLabel("Update Status")

                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BookingStatus.entries.filter { it != booking.status }.forEach { status ->
                                val statusColor = when (status) {
                                    BookingStatus.BOOKED     -> WCTheme.colors.statusBooked
                                    BookingStatus.SHOOT_DONE -> WCTheme.colors.statusShootDone
                                    BookingStatus.EDITING    -> WCTheme.colors.statusEditing
                                    BookingStatus.DELIVERED  -> WCTheme.colors.statusDelivered
                                    BookingStatus.CLOSED     -> WCTheme.colors.statusClosed
                                }
                                OutlinedButton(
                                    onClick = { onUpdateStatus(status) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = statusColor,
                                        containerColor = statusColor.copy(alpha = 0.08f)
                                    ),
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        status.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // RIGHT: Tasks
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookingDialogSectionLabel("Project Tasks")
                            IconButton(onClick = onAddTaskClick) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (isTasksLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else if (tasks.isEmpty()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    Text(
                                        "No tasks for this booking",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ── Footer ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss(); onExpenses() },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Expenses & P&L")
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
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BookingDialogSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
