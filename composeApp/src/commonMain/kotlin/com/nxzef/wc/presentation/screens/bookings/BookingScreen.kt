package com.nxzef.wc.presentation.screens.bookings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.screens.dashboard.StatusBadge
import com.nxzef.wc.presentation.screens.leads.WCDropdown
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.BookingStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
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
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bookings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.bookings.size} total bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.onAction(BookingAction.ShowCreateDialog)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("New Booking")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterStatus == null,
                        onClick = {
                            viewModel.onAction(
                                BookingAction.OnFilterStatus(null)
                            )
                        },
                        label = { Text("All") }
                    )
                }
                items(BookingStatus.entries) { status ->
                    FilterChip(
                        selected = state.filterStatus == status,
                        onClick = {
                            viewModel.onAction(
                                BookingAction.OnFilterStatus(status)
                            )
                        },
                        label = {
                            Text(status.name.replace("_", " "))
                        }
                    )
                }
            }

            HorizontalDivider()

            // Content
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                viewModel.filteredBookings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                            Text(
                                text = "No bookings found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.filteredBookings) { booking ->
                            BookingCard(
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

    // Booking detail dialog
    state.selectedBooking?.let { booking ->
        BookingDetailDialog(
            booking = booking,
            onDismiss = {
                viewModel.onAction(BookingAction.DismissDetail)
            },
            onUpdateStatus = { status ->
                viewModel.onAction(
                    BookingAction.OnUpdateStatus(booking.id, status)
                )
                viewModel.onAction(BookingAction.DismissDetail)
            }
        )
    }

    // Create booking dialog
    if (state.showCreateDialog) {
        CreateBookingDialog(
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    val statusColor = when (booking.status) {
        BookingStatus.BOOKED -> Color(0xFF2196F3)
        BookingStatus.SHOOT_DONE -> Color(0xFF009688)
        BookingStatus.EDITING -> Color(0xFFFF9800)
        BookingStatus.DELIVERED -> Color(0xFF8BC34A)
        BookingStatus.CLOSED -> Color(0xFF4CAF50)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    .height(60.dp),
                shape = RoundedCornerShape(4.dp),
                color = statusColor
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.eventType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "📍 ${booking.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📅 ${booking.eventDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            StatusBadge(status = booking.status.name)
        }
    }
}

@Composable
fun BookingDetailDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onUpdateStatus: (BookingStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${booking.eventType} — ${booking.eventDate}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Current Status")
                    StatusBadge(status = booking.status.name)
                }

                HorizontalDivider()

                Text(
                    text = "📍 ${booking.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "📅 ${booking.eventDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                booking.notes?.let {
                    Text(
                        text = "📝 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Update Status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // Status progression
                val nextStatuses = BookingStatus.entries
                    .filter { it != booking.status }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nextStatuses) { status ->
                        val color = when (status) {
                            BookingStatus.BOOKED -> Color(0xFF2196F3)
                            BookingStatus.SHOOT_DONE -> Color(0xFF009688)
                            BookingStatus.EDITING -> Color(0xFFFF9800)
                            BookingStatus.DELIVERED -> Color(0xFF8BC34A)
                            BookingStatus.CLOSED -> Color(0xFF4CAF50)
                        }
                        OutlinedButton(
                            onClick = { onUpdateStatus(status) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = color
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = status.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall
                            )
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
fun CreateBookingDialog(
    state: BookingState,
    onAction: (BookingAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(BookingAction.HideCreateDialog) },
        title = {
            Text(
                text = "Create Booking",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Select won lead
                WCDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Client (Won Lead) *",
                    selected = state.wonLeads
                        .find { it.id == state.selectedLeadId }
                        ?.fullName ?: "Select client",
                    options = state.wonLeads.map { it.fullName },
                    onSelect = { name ->
                        val lead = state.wonLeads
                            .find { it.fullName == name }
                        lead?.let {
                            onAction(BookingAction.OnLeadSelected(it.id))
                            onAction(
                                BookingAction.OnEventTypeChange(
                                    it.eventType.name
                                )
                            )
                            it.eventDate?.let { date ->
                                onAction(
                                    BookingAction.OnEventDateChange(date)
                                )
                            }
                        }
                    }
                )

                OutlinedTextField(
                    value = state.eventDate,
                    onValueChange = {
                        onAction(BookingAction.OnEventDateChange(it))
                    },
                    label = { Text("Event Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, null)
                    }
                )

                OutlinedTextField(
                    value = state.location,
                    onValueChange = {
                        onAction(BookingAction.OnLocationChange(it))
                    },
                    label = { Text("Location *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, null)
                    }
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        onAction(BookingAction.OnNotesChange(it))
                    },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAction(BookingAction.OnCreateBooking) },
                enabled = !state.isCreating
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Booking")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(BookingAction.HideCreateDialog) }
            ) {
                Text("Cancel")
            }
        }
    )
}