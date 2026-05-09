package com.nxzef.wc.presentation.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.LeadSourceBadge
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.presentation.components.RefreshButton
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.util.RefreshManager
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.ProjectPnL
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.shared.util.DateUtils
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onNavigateToPipeline: () -> Unit = {},
    onNavigateToInvoices: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAddLead: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onViewLeads: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    sessionManager: SessionManager = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by sessionManager.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DashboardUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is DashboardUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WCTopBar(
                title = "Executive Overview",
                subtitle = "Welcome, ${user?.name ?: "User"}",
                showNotificationIcon = true,
                modifier = Modifier.fillMaxWidth(),
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
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).widthIn(max = 600.dp).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onAction(DashboardAction.LoadStats) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Try Again")
                    }
                }

                state.stats != null -> {
                    DashboardContent(
                        stats = state.stats!!,
                        onNavigateToPipeline = onNavigateToPipeline,
                        onNavigateToInvoices = onNavigateToInvoices,
                        onNavigateToTasks = onNavigateToTasks,
                        onNavigateToAddLead = onNavigateToAddLead,
                        onNavigateToBookings = onNavigateToBookings,
                        onViewLeads = onViewLeads,
                        onSetGoalClick = { viewModel.onAction(DashboardAction.ShowGoalDialog) },
                        modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth()
                    )
                }
            }
        }
    }

    if (state.showGoalDialog) {
        MonthlyGoalDialog(
            revenue = state.goalRevenue,
            profit = state.goalProfit,
            isSaving = state.isSavingGoal,
            onRevenueChange = { viewModel.onAction(DashboardAction.OnGoalRevenueChange(it)) },
            onProfitChange = { viewModel.onAction(DashboardAction.OnGoalProfitChange(it)) },
            onConfirm = { viewModel.onAction(DashboardAction.SetMonthlyGoal) },
            onDismiss = { viewModel.onAction(DashboardAction.HideGoalDialog) }
        )
    }
}

@Composable
fun DashboardContent(
    stats: DashboardStats,
    onNavigateToPipeline: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAddLead: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onViewLeads: () -> Unit,
    onSetGoalClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Quick Actions Row
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionChip(
                            label = "Add Lead",
                            icon = Icons.Default.PersonAdd,
                            onClick = onNavigateToAddLead,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Lead Pipeline",
                            icon = Icons.Default.ViewColumn,
                            onClick = onNavigateToPipeline
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Upcoming Shoots",
                            icon = Icons.Default.CalendarToday,
                            onClick = onNavigateToBookings
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Invoices",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            onClick = onNavigateToInvoices
                        )
                    }
                    item {
                        QuickActionChip(
                            label = "Tasks",
                            icon = Icons.Default.AssignmentTurnedIn,
                            onClick = onNavigateToTasks
                        )
                    }
                }
            }
        }

        // Main KPIs
        item {
            androidx.compose.foundation.layout.BoxWithConstraints {
                val flowModifier = Modifier.fillMaxWidth()
                if (maxWidth > 600.dp) {
                    Row(
                        modifier = flowModifier,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Revenue (MTD)",
                            value = CurrencyUtils.formatINR(stats.totalRevenueThisMonth),
                            icon = Icons.Default.AccountBalanceWallet,
                            color = MaterialTheme.colorScheme.primary,
                            trend = "${if (stats.revenueTrendPercentage >= 0) "+" else ""}${stats.revenueTrendPercentage.toInt()}%",
                            isPositive = stats.revenueTrendPercentage >= 0
                        )
                        SummaryStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Conversion Rate",
                            value = "${stats.conversionRate.toInt()}%",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = WCTheme.colors.statusWon,
                            trend = "${if (stats.conversionRateTrendPercentage >= 0) "+" else ""}${stats.conversionRateTrendPercentage.toInt()}%",
                            isPositive = stats.conversionRateTrendPercentage >= 0
                        )
                        SummaryStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Avg. Order Value",
                            value = CurrencyUtils.formatINR(stats.averageOrderValue),
                            icon = Icons.Default.Analytics,
                            color = MaterialTheme.colorScheme.tertiary,
                            trend = "${if (stats.averageOrderValueTrendPercentage >= 0) "+" else ""}${stats.averageOrderValueTrendPercentage.toInt()}%",
                            isPositive = stats.averageOrderValueTrendPercentage >= 0
                        )
                    }
                } else {
                    Column(
                        modifier = flowModifier,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryStatCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Revenue (MTD)",
                            value = CurrencyUtils.formatINR(stats.totalRevenueThisMonth),
                            icon = Icons.Default.AccountBalanceWallet,
                            color = MaterialTheme.colorScheme.primary,
                            trend = "${if (stats.revenueTrendPercentage >= 0) "+" else ""}${stats.revenueTrendPercentage.toInt()}%",
                            isPositive = stats.revenueTrendPercentage >= 0
                        )
                        SummaryStatCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Conversion Rate",
                            value = "${stats.conversionRate.toInt()}%",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = WCTheme.colors.statusWon,
                            trend = "${if (stats.conversionRateTrendPercentage >= 0) "+" else ""}${stats.conversionRateTrendPercentage.toInt()}%",
                            isPositive = stats.conversionRateTrendPercentage >= 0
                        )
                        SummaryStatCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Avg. Order Value",
                            value = CurrencyUtils.formatINR(stats.averageOrderValue),
                            icon = Icons.Default.Analytics,
                            color = MaterialTheme.colorScheme.tertiary,
                            trend = "${if (stats.averageOrderValueTrendPercentage >= 0) "+" else ""}${stats.averageOrderValueTrendPercentage.toInt()}%",
                            isPositive = stats.averageOrderValueTrendPercentage >= 0
                        )
                    }
                }
            }
        }

        // Revenue Chart & Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Revenue Performance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily revenue trends for the current month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        FilledTonalButton(onClick = { /* Implement Export */ }) {
                            Text("Download Report")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        RevenueChart(
                            data = stats.revenueTrend.map { it.toFloat() },
                            color = MaterialTheme.colorScheme.primary,
                            targetGoal = stats.currentMonthGoal?.targetRevenue?.toFloat()
                        )
                    }
                }
            }
        }

        // Metrics Grid (Bookings, Leads, etc.)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Active Bookings",
                    value = stats.totalBookingsThisMonth.toString(),
                    icon = Icons.Default.EventAvailable,
                    color = MaterialTheme.colorScheme.secondary
                )
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Pending Invoices",
                    value = stats.pendingDeliveries.toString(),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    color = MaterialTheme.colorScheme.error
                )
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Open Leads",
                    value = stats.openLeads.toString(),
                    icon = Icons.Default.PersonSearch,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Source Breakdown & Pending Payments
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Lead Source Breakdown
                Card(
                    modifier = Modifier.weight(1.2f),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Lead Sources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        stats.leadsBySource.toList().sortedByDescending { it.second }.forEach { (source, count) ->
                            SourceRow(source = source, count = count, total = stats.openLeads)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Pending Payments
                Card(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Uncollected Revenue",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = CurrencyUtils.formatINR(stats.pendingPayments),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToInvoices,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Review Invoices")
                        }
                    }
                }
            }
        }

        // Recent Leads & Upcoming Bookings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Recent Leads
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Leads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onViewLeads) {
                            Text("View All")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stats.recentLeads.take(3).forEach { lead ->
                            PremiumLeadCard(
                                lead = lead,
                                onClick = onViewLeads
                            )
                        }
                    }
                }

                // Upcoming Bookings
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToBookings) {
                            Text("Calendar")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stats.upcomingBookings.take(3).forEach { booking ->
                            val lead = stats.recentLeads.find { it.id == booking.leadId }
                            BookingSummaryCard(booking = booking, clientName = lead?.fullName ?: "Client")
                        }
                    }
                }
            }
        }

        // Monthly P&L Section
        if (stats.projectPnLList.isNotEmpty() || stats.currentMonthGoal != null) {
            item {
                MonthlyPnLSection(
                    stats = stats,
                    onSetGoalClick = onSetGoalClick
                )
            }
        }
    }
}

@Composable
private fun MonthlyPnLSection(
    stats: DashboardStats,
    onSetGoalClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Monthly P&L",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSetGoalClick) {
                Text("Set Goal")
            }
        }

        if (stats.isMonthBelowTarget) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Below Monthly Target",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Actual ${CurrencyUtils.formatINR(stats.currentMonthActualProfit)} vs target ${CurrencyUtils.formatINR((stats.currentMonthGoal?.targetProfit ?: 0.0))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        stats.currentMonthGoal?.let { goal ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Target Revenue",
                    value = CurrencyUtils.formatINR(goal.targetRevenue),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.primary
                )
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Actual Profit",
                    value = CurrencyUtils.formatINR(stats.currentMonthActualProfit),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = if (stats.isMonthBelowTarget) MaterialTheme.colorScheme.error else WCTheme.colors.statusWon
                )
                KpiMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Target Profit",
                    value = CurrencyUtils.formatINR(goal.targetProfit),
                    icon = Icons.Default.Analytics,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        val topProjects = stats.projectPnLList.sortedByDescending { it.netProfit }.take(3)
        if (topProjects.isNotEmpty()) {
            Text("Top Projects", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            topProjects.forEach { pnl ->
                ProjectPnLRow(pnl = pnl)
            }
        }
    }
}

@Composable
private fun ProjectPnLRow(pnl: ProjectPnL) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pnl.eventType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(DateUtils.formatDisplayDate(pnl.eventDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyUtils.formatINR(pnl.netProfit),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pnl.netProfit >= 0) WCTheme.colors.statusWon else MaterialTheme.colorScheme.error
                )
                Text(
                    "${pnl.marginPercent.toInt()}% margin",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthlyGoalDialog(
    revenue: String,
    profit: String,
    isSaving: Boolean,
    onRevenueChange: (String) -> Unit,
    onProfitChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 420.dp),
        title = { Text("Set Monthly Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = revenue,
                    onValueChange = onRevenueChange,
                    label = { Text("Target Revenue (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = profit,
                    onValueChange = onProfitChange,
                    label = { Text("Target Profit (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (isSaving) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BookingSummaryCard(booking: Booking, clientName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val dateParts = booking.eventDate.split("-")
                    if (dateParts.size >= 3) {
                        Text(
                            text = dateParts[2],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = dateParts[1],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = booking.eventType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = contentColor)
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Composable
fun SummaryStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String,
    isPositive: Boolean
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = (if (isPositive) WCTheme.colors.statusWon else MaterialTheme.colorScheme.error).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = trend,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) WCTheme.colors.statusWon else MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KpiMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SourceRow(source: String, count: Int, total: Int) {
    val percentage = if (total > 0) count.toFloat() / total else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LeadSourceBadge(source = try { LeadSource.valueOf(source.uppercase()) } catch(e: Exception) { LeadSource.OTHER })
                Spacer(Modifier.width(8.dp))
                Text(source, style = MaterialTheme.typography.bodyMedium)
            }
            Text("$count", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
fun PremiumLeadCard(
    lead: Lead,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lead.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(lead.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${lead.eventType.name} • ${lead.eventDate ?: "Date TBD"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                LeadStatusBadge(statusName = lead.statusName, color = lead.customStatus?.color)
                Spacer(Modifier.height(4.dp))
                LeadSourceBadge(source = lead.source)
            }
        }
    }
}

@Composable
fun RevenueChart(
    data: List<Float>,
    color: Color,
    targetGoal: Float? = null
) {
    // Use rememberSaveable to ensure animation only runs once per screen session
    var animationPlayed by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (animationPlayed) 1f else 0f) }
    
    LaunchedEffect(Unit) {
        if (!animationPlayed) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
            animationPlayed = true
        }
    }

    val maxVal = (data.maxOrNull() ?: 1f).coerceAtLeast(targetGoal ?: 0f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1).coerceAtLeast(1)

        // Draw horizontal grid lines
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = height - (i * height / gridLines)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw Target Goal Line (Red)
        targetGoal?.let { goal ->
            val yGoal = height - (goal / maxVal * height)
            drawLine(
                color = Color.Red.copy(alpha = 0.6f),
                start = Offset(0f, yGoal),
                end = Offset(width, yGoal),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * spacing
            val normalizedValue = if (maxVal > 0) value / maxVal else 0f
            val y = height - (normalizedValue * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        val clipWidth = width * progress.value
        
        drawContext.canvas.save()
        drawContext.canvas.clipRect(0f, 0f, clipWidth, height)

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        
        data.forEachIndexed { index, value ->
            val x = index * spacing
            val normalizedValue = if (maxVal > 0) value / maxVal else 0f
            if (x <= clipWidth) {
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = Offset(x, height - (normalizedValue * height))
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, height - (normalizedValue * height))
                )
            }
        }
        
        drawContext.canvas.restore()
    }
}

fun formatCurrency(amount: Double): String {
    return if (amount >= 1000) {
        "${(amount / 1000).toInt()}k"
    } else {
        amount.toInt().toString()
    }
}
