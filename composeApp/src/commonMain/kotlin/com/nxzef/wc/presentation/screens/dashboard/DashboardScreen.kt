package com.nxzef.wc.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.data.model.Booking
import com.nxzef.wc.data.model.DashboardStats
import com.nxzef.wc.data.model.Lead
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Row(modifier = Modifier.fillMaxSize()) {

        // Sidebar
        DashboardSidebar(onLogout = onLogout)

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load dashboard",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::loadStats) {
                            Text("Retry")
                        }
                    }
                }
                state.stats != null -> {
                    DashboardContent(stats = state.stats!!)
                }
            }
        }
    }
}

@Composable
fun DashboardSidebar(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Brand
            Text(
                text = "☁️ WC",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SidebarItem(
                icon     = Icons.Default.Dashboard,
                label    = "Dashboard",
                selected = true
            )
            SidebarItem(
                icon     = Icons.Default.People,
                label    = "Leads",
                selected = false
            )
            SidebarItem(
                icon     = Icons.Default.CalendarMonth,
                label    = "Bookings",
                selected = false
            )
            SidebarItem(
                icon     = Icons.Default.Receipt,
                label    = "Invoices",
                selected = false
            )
            SidebarItem(
                icon     = Icons.Default.Group,
                label    = "Team",
                selected = false
            )
            SidebarItem(
                icon     = Icons.Default.Settings,
                label    = "Settings",
                selected = false
            )
        }

        // Logout
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text  = "Logout",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SidebarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    val background = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else Color.Transparent

    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = contentColor,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text      = label,
            fontSize  = 14.sp,
            color     = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold
            else FontWeight.Normal
        )
    }
}

@Composable
fun DashboardContent(stats: DashboardStats) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        item {
            Text(
                text       = "Dashboard",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text  = "Welcome back, Niyas! Here's what's happening.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // KPI Cards
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(
                    modifier    = Modifier.weight(1f),
                    title       = "Revenue This Month",
                    value       = "₹${stats.totalRevenueThisMonth.toLong()}",
                    icon        = Icons.Default.CurrencyRupee,
                    color       = Color(0xFF4CAF50)
                )
                KpiCard(
                    modifier    = Modifier.weight(1f),
                    title       = "Bookings This Month",
                    value       = stats.totalBookingsThisMonth.toString(),
                    icon        = Icons.Default.CalendarMonth,
                    color       = Color(0xFF2196F3)
                )
                KpiCard(
                    modifier    = Modifier.weight(1f),
                    title       = "Open Leads",
                    value       = stats.openLeads.toString(),
                    icon        = Icons.Default.People,
                    color       = Color(0xFFFF9800)
                )
                KpiCard(
                    modifier    = Modifier.weight(1f),
                    title       = "Pending Deliveries",
                    value       = stats.pendingDeliveries.toString(),
                    icon        = Icons.Default.Pending,
                    color       = Color(0xFFE91E63)
                )
            }
        }

        // Pending payments card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "💸 Pending Payments",
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text       = "₹${stats.pendingPayments.toLong()}",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Lead sources
        if (stats.leadsBySource.isNotEmpty()) {
            item {
                Text(
                    text       = "Leads by Source",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stats.leadsBySource.forEach { (source, count) ->
                        SourceChip(source = source, count = count)
                    }
                }
            }
        }

        // Recent Leads
        if (stats.recentLeads.isNotEmpty()) {
            item {
                Text(
                    text       = "Recent Leads",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(stats.recentLeads) { lead ->
                RecentLeadCard(lead = lead)
            }
        }

        // Upcoming Bookings
        if (stats.upcomingBookings.isNotEmpty()) {
            item {
                Text(
                    text       = "Upcoming Shoots",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(stats.upcomingBookings) { booking ->
                UpcomingBookingCard(booking = booking)
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier : Modifier = Modifier,
    title    : String,
    value    : String,
    icon     : ImageVector,
    color    : Color
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.SpaceBetween,
                modifier               = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = title,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = value,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
        }
    }
}

@Composable
fun SourceChip(source: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = source,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color    = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text     = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color    = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RecentLeadCard(lead: Lead) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = lead.fullName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
                Text(
                    text     = "${lead.eventType} • ${lead.source}",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                lead.eventDate?.let {
                    Text(
                        text     = it,
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusBadge(status = lead.status)
        }
    }
}

@Composable
fun UpcomingBookingCard(booking: Booking) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = booking.eventType,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
                Text(
                    text     = booking.location,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text     = booking.eventDate,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.primary
                )
            }
            StatusBadge(status = booking.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "NEW"         -> Color(0xFF2196F3)
        "CONTACTED"   -> Color(0xFFFF9800)
        "NEGOTIATING" -> Color(0xFF9C27B0)
        "WON"         -> Color(0xFF4CAF50)
        "LOST"        -> Color(0xFFF44336)
        "BOOKED"      -> Color(0xFF2196F3)
        "SHOOT_DONE"  -> Color(0xFF009688)
        "EDITING"     -> Color(0xFFFF9800)
        "DELIVERED"   -> Color(0xFF8BC34A)
        "CLOSED"      -> Color(0xFF4CAF50)
        else          -> Color(0xFF9E9E9E)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text     = status,
            fontSize = 11.sp,
            color    = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}