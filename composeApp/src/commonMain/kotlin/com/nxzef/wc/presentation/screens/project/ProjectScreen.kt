package com.nxzef.wc.presentation.screens.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.BookingStatusBadge
import com.nxzef.wc.presentation.components.StatusBadge
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.screens.leads.WCDropdown
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.EXPENSE_CATEGORIES
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.ProjectExpense
import com.nxzef.wc.shared.model.Task
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.shared.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    bookingId: String,
    onBack: () -> Unit,
    onExpenses: (bookingId: String) -> Unit,
    onInvoice: () -> Unit,
    viewModel: ProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(bookingId) {
        viewModel.onAction(ProjectAction.Load(bookingId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProjectUiEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = state.lead?.fullName ?: state.booking?.eventType ?: "Project",
                subtitle = state.booking?.let { "${it.eventType} · ${DateUtils.formatDisplayDate(it.eventDate)}" },
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.onAction(ProjectAction.Retry) }) {
                            Text("Retry")
                        }
                    }
                }
                state.booking != null -> {
                    val booking = state.booking!!
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 1000.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ProjectHeaderCard(
                                booking = booking,
                                lead = state.lead,
                                team = state.team,
                                onUpdateStatus = { viewModel.onAction(ProjectAction.ShowAssignTeamDialog) },
                                onAssignTeam = { viewModel.onAction(ProjectAction.ShowAssignTeamDialog) },
                                onViewInvoice = onInvoice
                            )
                        }
                        item {
                            ProjectSection("Project Timeline") {
                                ProjectTimelineStepper(
                                    currentStatus = booking.status,
                                    onStageClick = { status ->
                                        viewModel.onAction(ProjectAction.RequestStatusChange(status))
                                    }
                                )
                            }
                        }
                        item {
                            ProjectSection("Team Tasks") {
                                ProjectTasksContent(
                                    tasks = state.tasks,
                                    team = state.team,
                                    onTaskToggle = { taskId, isDone ->
                                        viewModel.onAction(ProjectAction.OnTaskToggle(taskId, isDone))
                                    }
                                )
                            }
                        }
                        item {
                            ProjectSection("Financial Summary") {
                                ProjectFinancialContent(
                                    invoice = state.invoice,
                                    totalExpenses = state.expenses.sumOf { it.actualAmount },
                                    onViewInvoice = onInvoice
                                )
                            }
                        }
                        item {
                            ProjectSection("Expenses") {
                                ProjectExpensesPreview(
                                    expenses = state.expenses,
                                    onAddExpense = { viewModel.onAction(ProjectAction.ShowAddExpenseDialog) },
                                    onViewAll = { onExpenses(bookingId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Status change confirmation dialog
    if (state.showStatusDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(ProjectAction.DismissStatusDialog) },
            title = { Text("Change Status") },
            text = {
                Text(
                    "Move to ${state.pendingStatus?.name?.replace("_", " ")}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(ProjectAction.ConfirmStatusChange) }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(ProjectAction.DismissStatusDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Assign team dialog
    if (state.showAssignTeamDialog) {
        ProjectAssignTeamDialog(state = state, viewModel = viewModel)
    }

    // Add expense dialog
    if (state.showAddExpenseDialog) {
        ProjectAddExpenseDialog(state = state, viewModel = viewModel)
    }
}

// ── Section 1: Header Card ────────────────────────────────────────────────────

@Composable
private fun ProjectHeaderCard(
    booking: Booking,
    lead: Lead?,
    team: List<User>,
    onUpdateStatus: () -> Unit,
    onAssignTeam: () -> Unit,
    onViewInvoice: () -> Unit
) {
    val photographer = team.find { it.id == booking.photographerId }
    val editor = team.find { it.id == booking.editorId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = lead?.fullName ?: "Unknown Client",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = booking.eventType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📅 ${DateUtils.formatDisplayDate(booking.eventDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📍 ${booking.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BookingStatusBadge(status = booking.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamAssigneeChip(
                    role = "Photographer",
                    name = photographer?.name ?: "Unassigned"
                )
                TeamAssigneeChip(
                    role = "Editor",
                    name = editor?.name ?: "Unassigned"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUpdateStatus,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Update Status", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onAssignTeam,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Assign Team", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onViewInvoice,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View Invoice", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun TeamAssigneeChip(role: String, name: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Section 2: Timeline Stepper ───────────────────────────────────────────────

@Composable
private fun ProjectTimelineStepper(
    currentStatus: BookingStatus,
    onStageClick: (BookingStatus) -> Unit
) {
    val stages = BookingStatus.entries
    val currentIdx = stages.indexOf(currentStatus)
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = Modifier.fillMaxWidth()) {
        // Connecting lines drawn on canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            val count = stages.size
            val stepW = size.width / count
            val cy = size.height / 2f
            val r = 18.dp.toPx()
            for (i in 0 until count - 1) {
                val x1 = i * stepW + stepW / 2f + r
                val x2 = (i + 1) * stepW + stepW / 2f - r
                drawLine(
                    color = if (i < currentIdx) primary else outline.copy(alpha = 0.5f),
                    start = Offset(x1, cy),
                    end = Offset(x2, cy),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Step circles and labels
        Row(modifier = Modifier.fillMaxWidth()) {
            stages.forEachIndexed { idx, stage ->
                val isCompleted = idx < currentIdx
                val isCurrent = idx == currentIdx

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (!isCurrent) Modifier.clickable { onStageClick(stage) } else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isCompleted || isCurrent) primary else surface,
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                if (isCompleted || isCurrent) primary else outline,
                                CircleShape
                            )
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = onPrimary
                            )
                        } else {
                            Text(
                                "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) onPrimary else outline
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stage.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = if (isCurrent) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// ── Section 3: Team Tasks ─────────────────────────────────────────────────────

@Composable
private fun ProjectTasksContent(
    tasks: List<Task>,
    team: List<User>,
    onTaskToggle: (String, Boolean) -> Unit
) {
    if (tasks.isEmpty()) {
        Text(
            "No tasks for this project",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val grouped = tasks.groupBy { it.assignedTo }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        grouped.forEach { (userId, userTasks) ->
            val user = team.find { it.id == userId }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.name ?: "Unknown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (user != null) {
                        val roleColor = when (user.role) {
                            UserRole.OWNER -> WCTheme.colors.statusBooked
                            UserRole.LEAD_MANAGER -> WCTheme.colors.statusEditing
                            UserRole.MARKETING -> WCTheme.colors.statusShootDone
                            UserRole.PHOTOGRAPHER -> WCTheme.colors.statusDelivered
                            UserRole.EDITOR -> WCTheme.colors.statusClosed
                        }
                        StatusBadge(
                            text = user.role.name.replace("_", " "),
                            color = roleColor
                        )
                    }
                }
                userTasks.forEach { task ->
                    TaskCheckItem(
                        task = task,
                        onToggle = { onTaskToggle(task.id, it) },
                        onDelete = {}
                    )
                }
            }
        }
    }
}

// ── Section 4: Financial Summary ──────────────────────────────────────────────

@Composable
private fun ProjectFinancialContent(
    invoice: Invoice?,
    totalExpenses: Double,
    onViewInvoice: () -> Unit
) {
    val wcColors = WCTheme.colors

    if (invoice == null) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "No invoice created for this project.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onViewInvoice, shape = MaterialTheme.shapes.medium) {
                Text("View Invoices")
            }
        }
        return
    }

    val netProfit = invoice.totalAmount - totalExpenses

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FinancialRow("Invoice Total", CurrencyUtils.formatINR(invoice.totalAmount))
        FinancialRow(
            label = "Deposit (${CurrencyUtils.formatINR(invoice.depositAmount)})",
            value = if (invoice.depositPaid) "✓ Paid${invoice.depositPaidDate?.let { " on $it" } ?: ""}"
                    else "Pending",
            valueColor = if (invoice.depositPaid) wcColors.success else wcColors.warning
        )
        FinancialRow(
            label = "Final Payment",
            value = if (invoice.finalPaid) "✓ Paid${invoice.finalPaidDate?.let { " on $it" } ?: ""}"
                    else "Pending",
            valueColor = if (invoice.finalPaid) wcColors.success else wcColors.warning
        )
        FinancialRow(
            label = "Remaining",
            value = CurrencyUtils.formatINR(invoice.remainingAmount),
            valueColor = if (invoice.remainingAmount > 0) MaterialTheme.colorScheme.error else wcColors.success
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        FinancialRow("Total Revenue", CurrencyUtils.formatINR(invoice.totalAmount),
                     valueColor = MaterialTheme.colorScheme.primary)
        FinancialRow("Total Expenses", CurrencyUtils.formatINR(totalExpenses),
                     valueColor = MaterialTheme.colorScheme.error)
        FinancialRow(
            label = "Net Profit",
            value = CurrencyUtils.formatINR(netProfit),
            labelWeight = FontWeight.Bold,
            valueColor = if (netProfit >= 0) wcColors.success else MaterialTheme.colorScheme.error,
            valueFontWeight = FontWeight.Bold
        )

        TextButton(onClick = onViewInvoice, shape = MaterialTheme.shapes.medium) {
            Text("View Full Invoice →")
        }
    }
}

@Composable
private fun FinancialRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelWeight: FontWeight = FontWeight.Normal,
    valueFontWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = labelWeight,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = valueFontWeight,
            color = valueColor
        )
    }
}

// ── Section 5: Expenses Preview ───────────────────────────────────────────────

@Composable
private fun ProjectExpensesPreview(
    expenses: List<ProjectExpense>,
    onAddExpense: () -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${expenses.size} expense${if (expenses.size == 1) "" else "s"} · " +
                        CurrencyUtils.formatINR(expenses.sumOf { it.actualAmount }) + " total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onAddExpense, shape = MaterialTheme.shapes.medium) {
                Text("+ Add Expense")
            }
        }

        if (expenses.isEmpty()) {
            Text(
                "No expenses recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            expenses.takeLast(3).forEach { expense ->
                ExpensePreviewRow(expense = expense)
            }
        }

        TextButton(
            onClick = onViewAll,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("View All Expenses →")
        }
    }
}

@Composable
private fun ExpensePreviewRow(expense: ProjectExpense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.category,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            expense.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                CurrencyUtils.formatINR(expense.actualAmount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                expense.expenseDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Shared section wrapper ────────────────────────────────────────────────────

@Composable
private fun ProjectSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
private fun ProjectAssignTeamDialog(
    state: ProjectState,
    viewModel: ProjectViewModel
) {
    val photographers = state.team.filter { it.role == UserRole.PHOTOGRAPHER }
    val editors = state.team.filter { it.role == UserRole.EDITOR }

    val currentPhotographerName = state.team.find { it.id == state.draftPhotographerId }?.name ?: "Unassigned"
    val currentEditorName = state.team.find { it.id == state.draftEditorId }?.name ?: "Unassigned"

    AlertDialog(
        onDismissRequest = { viewModel.onAction(ProjectAction.HideAssignTeamDialog) },
        modifier = Modifier.widthIn(max = 480.dp),
        title = { Text("Assign Team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WCDropdown(
                    label = "Photographer",
                    selected = currentPhotographerName,
                    options = listOf("Unassigned") + photographers.map { it.name },
                    onSelect = { name ->
                        if (name == "Unassigned") viewModel.onAction(ProjectAction.SelectPhotographer(null))
                        else photographers.find { it.name == name }?.let {
                            viewModel.onAction(ProjectAction.SelectPhotographer(it.id))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                WCDropdown(
                    label = "Editor",
                    selected = currentEditorName,
                    options = listOf("Unassigned") + editors.map { it.name },
                    onSelect = { name ->
                        if (name == "Unassigned") viewModel.onAction(ProjectAction.SelectEditor(null))
                        else editors.find { it.name == name }?.let {
                            viewModel.onAction(ProjectAction.SelectEditor(it.id))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.onAction(ProjectAction.ConfirmAssignTeam) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onAction(ProjectAction.HideAssignTeamDialog) }) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectAddExpenseDialog(
    state: ProjectState,
    viewModel: ProjectViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.onAction(ProjectAction.HideAddExpenseDialog) },
        modifier = Modifier.widthIn(max = 520.dp),
        title = { Text("Add Expense") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WCDropdown(
                    label = "Category",
                    selected = state.newExpCategory,
                    options = EXPENSE_CATEGORIES,
                    onSelect = { viewModel.onAction(ProjectAction.OnExpCategoryChange(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.newExpDescription,
                    onValueChange = { viewModel.onAction(ProjectAction.OnExpDescriptionChange(it)) },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newExpEstimated,
                        onValueChange = { viewModel.onAction(ProjectAction.OnExpEstimatedChange(it)) },
                        label = { Text("Estimated ₹") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.newExpActual,
                        onValueChange = { viewModel.onAction(ProjectAction.OnExpActualChange(it)) },
                        label = { Text("Actual ₹ *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = state.newExpDate,
                    onValueChange = { viewModel.onAction(ProjectAction.OnExpDateChange(it)) },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.newExpPaymentMethod,
                    onValueChange = { viewModel.onAction(ProjectAction.OnExpPaymentMethodChange(it)) },
                    label = { Text("Payment Method (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.newExpNotes,
                    onValueChange = { viewModel.onAction(ProjectAction.OnExpNotesChange(it)) },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                if (state.isSavingExpense) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.onAction(ProjectAction.AddExpense) },
                enabled = !state.isSavingExpense
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onAction(ProjectAction.HideAddExpenseDialog) }) {
                Text("Cancel")
            }
        }
    )
}
