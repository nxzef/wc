package com.nxzef.wc.presentation.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.InvoiceStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.presentation.screens.leads.WCDropdown
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onBack: () -> Unit,
    viewModel: InvoiceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is InvoiceUiEvent.ShowSnackbar ->
                    snackbarState.showSnackbar(event.message)

                InvoiceUiEvent.InvoiceCreated ->
                    snackbarState.showSnackbar("Invoice created!")

                InvoiceUiEvent.PaymentUpdated ->
                    snackbarState.showSnackbar("Payment updated!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Invoices",
                subtitle = "${state.invoices.size} invoices",
                onBack = onBack,
                actions = {
                    Button(
                        onClick = {
                            viewModel.onAction(InvoiceAction.ShowCreateDialog)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("New Invoice")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.invoices.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No invoices yet",
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
                        item {
                            InvoiceSummaryRow(invoices = state.invoices)
                        }
                        items(state.invoices) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
                                onClick = {
                                    viewModel.onAction(
                                        InvoiceAction.SelectInvoice(invoice)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Invoice detail
    state.selectedInvoice?.let { invoice ->
        InvoiceDetailDialog(
            invoice = invoice,
            onDismiss = {
                viewModel.onAction(InvoiceAction.DismissDetail)
            },
            onMarkDeposit = {
                viewModel.onAction(
                    InvoiceAction.OnMarkDepositPaid(
                        invoice.id,
                        DateUtils.getCurrentDateIso()
                    )
                )
                viewModel.onAction(InvoiceAction.DismissDetail)
            },
            onMarkFinal = {
                viewModel.onAction(
                    InvoiceAction.OnMarkFinalPaid(
                        invoice.id,
                        DateUtils.getCurrentDateIso()
                    )
                )
                viewModel.onAction(InvoiceAction.DismissDetail)
            }
        )
    }

    // Create dialog
    if (state.showCreateDialog) {
        CreateInvoiceDialog(
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun InvoiceSummaryRow(invoices: List<Invoice>) {
    val totalRevenue = invoices.sumOf { it.totalAmount }
    val collected = invoices.sumOf {
        when {
            it.finalPaid -> it.totalAmount
            it.depositPaid -> it.depositAmount
            else -> 0.0
        }
    }
    val pending = totalRevenue - collected

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Total Revenue",
            value = "₹${totalRevenue.toLong()}",
            color = MaterialTheme.colorScheme.primary
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Collected",
            value = "₹${collected.toLong()}",
            color = WCTheme.colors.statusWon
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Pending",
            value = "₹${pending.toLong()}",
            color = WCTheme.colors.statusLost
        )
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val paymentStatus = when {
        invoice.finalPaid -> "FULLY PAID"
        invoice.depositPaid -> "DEPOSIT PAID"
        else -> "UNPAID"
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "₹${invoice.totalAmount.toLong()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Deposit: ₹${invoice.depositAmount.toLong()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Remaining: ₹${invoice.remainingAmount.toLong()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (invoice.remainingAmount > 0)
                        MaterialTheme.colorScheme.error
                    else
                        WCTheme.colors.statusWon
                )
            }

            InvoiceStatusBadge(status = paymentStatus)
        }
    }
}

@Composable
fun InvoiceDetailDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onMarkDeposit: () -> Unit,
    onMarkFinal: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Invoice Details",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailInvoiceRow(
                    "Total Amount",
                    "₹${invoice.totalAmount.toLong()}"
                )
                DetailInvoiceRow(
                    "Deposit",
                    "₹${invoice.depositAmount.toLong()}"
                )
                DetailInvoiceRow(
                    "Remaining",
                    "₹${invoice.remainingAmount.toLong()}"
                )

                HorizontalDivider()

                // Deposit payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Deposit",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = if (invoice.depositPaid)
                                "Paid ${invoice.depositPaidDate ?: ""}"
                            else "Pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (invoice.depositPaid)
                                WCTheme.colors.statusWon
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    if (!invoice.depositPaid) {
                        FilledTonalButton(onClick = onMarkDeposit) {
                            Text("Mark Paid")
                        }
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WCTheme.colors.statusWon
                        )
                    }
                }

                // Final payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Final Payment",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = if (invoice.finalPaid)
                                "Paid ${invoice.finalPaidDate ?: ""}"
                            else "Pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (invoice.finalPaid)
                                WCTheme.colors.statusWon
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    if (!invoice.finalPaid && invoice.depositPaid) {
                        FilledTonalButton(onClick = onMarkFinal) {
                            Text("Mark Paid")
                        }
                    } else if (invoice.finalPaid) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WCTheme.colors.statusWon
                        )
                    }
                }

                invoice.notes?.let {
                    HorizontalDivider()
                    Text(
                        text = "📝 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
fun DetailInvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CreateInvoiceDialog(
    state: InvoiceState,
    onAction: (InvoiceAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(InvoiceAction.HideCreateDialog) },
        title = {
            Text(
                text = "Create Invoice",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WCDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Booking *",
                    selected = state.bookings
                        .find { it.id == state.selectedBookingId }
                        ?.let { "${it.eventType} — ${it.eventDate}" }
                        ?: "Select booking",
                    options = state.bookings
                        .map { "${it.eventType} — ${it.eventDate}" },
                    onSelect = { display ->
                        val booking = state.bookings.find {
                            "${it.eventType} — ${it.eventDate}" == display
                        }
                        booking?.let {
                            onAction(
                                InvoiceAction.OnBookingSelected(it.id)
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = state.totalAmount,
                    onValueChange = {
                        onAction(InvoiceAction.OnTotalAmountChange(it))
                    },
                    label = { Text("Total Amount (₹) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyRupee, null)
                    }
                )

                OutlinedTextField(
                    value = state.depositAmount,
                    onValueChange = {
                        onAction(InvoiceAction.OnDepositAmountChange(it))
                    },
                    label = { Text("Deposit Amount (₹) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyRupee, null)
                    }
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        onAction(InvoiceAction.OnNotesChange(it))
                    },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAction(InvoiceAction.OnCreateInvoice) },
                enabled = !state.isCreating
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(InvoiceAction.HideCreateDialog) }
            ) { Text("Cancel") }
        }
    )
}