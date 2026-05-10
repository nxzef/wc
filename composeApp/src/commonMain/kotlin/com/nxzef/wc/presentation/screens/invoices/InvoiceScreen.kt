package com.nxzef.wc.presentation.screens.invoices

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.InvoiceStatusBadge
import com.nxzef.wc.presentation.components.RefreshButton
import com.nxzef.wc.presentation.components.WCSearchBar
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.util.RefreshManager
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.Receipt
import com.nxzef.wc.shared.model.ReceiptType
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
                is InvoiceUiEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                InvoiceUiEvent.PaymentUpdated  -> snackbarState.showSnackbar("Payment updated!")
                InvoiceUiEvent.InvoiceCreated  -> snackbarState.showSnackbar("Invoice created!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Invoices",
                subtitle = "${state.invoices.size} total",
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
            Column(modifier = Modifier.widthIn(max = 1000.dp).fillMaxSize()) {
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.invoices.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No invoices yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        val filteredInvoices = remember(
                            state.invoices, state.searchQuery, state.bookings, state.leads
                        ) {
                            if (state.searchQuery.isBlank()) state.invoices
                            else {
                                val q = state.searchQuery.trim()
                                state.invoices.filter { invoice ->
                                    val booking = state.bookings.find { it.id == invoice.bookingId }
                                    val lead = booking?.let { b -> state.leads.find { it.id == b.leadId } }
                                    lead?.fullName?.contains(q, ignoreCase = true) == true ||
                                        booking?.eventType?.contains(q, ignoreCase = true) == true ||
                                        booking?.eventDate?.contains(q, ignoreCase = true) == true ||
                                        invoice.notes?.contains(q, ignoreCase = true) == true
                                }
                            }
                        }

                        WCSearchBar(
                            query = state.searchQuery,
                            onQueryChange = { viewModel.onAction(InvoiceAction.OnSearchQueryChange(it)) },
                            placeholder = "Search invoices…",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                InvoiceSummaryRow(invoices = state.invoices)
                            }
                            if (filteredInvoices.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Receipt, null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            )
                                            Text(
                                                "No invoices match your search",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(filteredInvoices) { invoice ->
                                    val booking = state.bookings.find { it.id == invoice.bookingId }
                                    val lead = booking?.let { b -> state.leads.find { it.id == b.leadId } }
                                    InvoiceCard(
                                        invoice = invoice,
                                        booking = booking,
                                        lead = lead,
                                        onClick = { viewModel.onAction(InvoiceAction.SelectInvoice(invoice)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedInvoice?.let { invoice ->
        val booking = state.bookings.find { it.id == invoice.bookingId }
        InvoiceDetailDialog(
            invoice = invoice,
            booking = booking,
            receipts = state.receipts,
            isLoadingReceipts = state.isLoadingReceipts,
            onDismiss = { viewModel.onAction(InvoiceAction.DismissDetail) },
            onMarkDeposit = {
                viewModel.onAction(InvoiceAction.OnMarkDepositPaid(invoice.id, DateUtils.getCurrentDateIso()))
            },
            onMarkFinal = {
                viewModel.onAction(InvoiceAction.OnMarkFinalPaid(invoice.id, DateUtils.getCurrentDateIso()))
            },
            onViewReceipt = { receipt -> viewModel.onAction(InvoiceAction.ViewReceipt(receipt)) }
        )
    }

    state.viewingReceipt?.let { receipt ->
        val booking = state.bookings.find { it.id == receipt.bookingId }
        ReceiptViewerDialog(
            receipt = receipt,
            booking = booking,
            onDismiss = { viewModel.onAction(InvoiceAction.DismissReceipt) }
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
        SummaryCard(Modifier.weight(1f), "Total Revenue", CurrencyUtils.formatINR(totalRevenue), MaterialTheme.colorScheme.primary, true)
        SummaryCard(Modifier.weight(1f), "Collected", CurrencyUtils.formatINR(collected), WCTheme.colors.statusWon, false)
        SummaryCard(Modifier.weight(1f), "Pending", CurrencyUtils.formatINR(pending), WCTheme.colors.statusLost, false)
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, label: String, value: String, color: Color, isPrimary: Boolean) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, booking: Booking?, lead: Lead?, onClick: () -> Unit) {
    val paymentStatus = when {
        invoice.finalPaid -> "FULLY PAID"
        invoice.depositPaid -> "DEPOSIT PAID"
        else -> "UNPAID"
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                lead?.let {
                    Text(
                        text = it.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                booking?.let {
                    Text(
                        text = "${it.eventType} • ${it.eventDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(CurrencyUtils.formatINR(invoice.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Deposit: ${CurrencyUtils.formatINR(invoice.depositAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Remaining: ${CurrencyUtils.formatINR(invoice.remainingAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (invoice.remainingAmount > 0) MaterialTheme.colorScheme.error else WCTheme.colors.statusWon
                )
            }
            InvoiceStatusBadge(status = paymentStatus)
        }
    }
}

@Composable
fun InvoiceDetailDialog(
    invoice: Invoice,
    booking: Booking?,
    receipts: List<Receipt>,
    isLoadingReceipts: Boolean,
    onDismiss: () -> Unit,
    onMarkDeposit: () -> Unit,
    onMarkFinal: () -> Unit,
    onViewReceipt: (Receipt) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Invoice Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                booking?.let {
                    Text(
                        text = "${it.eventType} · ${it.eventDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                DetailInvoiceRow("Total Amount", CurrencyUtils.formatINR(invoice.totalAmount))
                DetailInvoiceRow("Deposit", CurrencyUtils.formatINR(invoice.depositAmount))
                DetailInvoiceRow("Remaining", CurrencyUtils.formatINR(invoice.remainingAmount))

                HorizontalDivider()

                // Deposit payment row
                PaymentRow(
                    label = "Deposit",
                    isPaid = invoice.depositPaid,
                    paidDate = invoice.depositPaidDate,
                    canMark = !invoice.depositPaid,
                    onMark = onMarkDeposit
                )

                // Final payment row
                PaymentRow(
                    label = "Final Payment",
                    isPaid = invoice.finalPaid,
                    paidDate = invoice.finalPaidDate,
                    canMark = !invoice.finalPaid && invoice.depositPaid,
                    onMark = onMarkFinal
                )

                invoice.notes?.let {
                    HorizontalDivider()
                    Text(text = " $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Payment History section
                if (isLoadingReceipts) {
                    HorizontalDivider()
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                } else if (receipts.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    receipts.forEach { receipt ->
                        ReceiptHistoryRow(receipt = receipt, onView = { onViewReceipt(receipt) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun PaymentRow(
    label: String,
    isPaid: Boolean,
    paidDate: String?,
    canMark: Boolean,
    onMark: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = if (isPaid) "Paid ${paidDate ?: ""}" else "Pending",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPaid) WCTheme.colors.statusWon else MaterialTheme.colorScheme.error
            )
        }
        if (canMark) {
            FilledTonalButton(onClick = onMark, shape = MaterialTheme.shapes.medium) {
                Text("Mark Paid")
            }
        } else if (isPaid) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WCTheme.colors.statusWon)
        }
    }
}

@Composable
private fun ReceiptHistoryRow(receipt: Receipt, onView: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (receipt.receiptType == ReceiptType.ADVANCE) "Advance Receipt" else "Final Receipt",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${CurrencyUtils.formatINR(receipt.amount)} · ${receipt.paidDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = onView,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" View", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ReceiptViewerDialog(
    receipt: Receipt,
    booking: Booking?,
    onDismiss: () -> Unit
) {
    val typeLabel = if (receipt.receiptType == ReceiptType.ADVANCE) "Advance Payment" else "Final Payment"
    val shortId = receipt.id.take(8).uppercase()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Receipt", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "The Wedding Clouds",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

                        ReceiptLine("Receipt #", shortId)
                        booking?.let { ReceiptLine("Client", it.eventType) }
                        ReceiptLine("Date", receipt.paidDate)
                        ReceiptLine("Type", typeLabel)
                        ReceiptLine("Amount", CurrencyUtils.formatINR(receipt.amount))
                    }
                }

                Text(
                    text = "Thank you for choosing The Wedding Clouds for your special day!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetailInvoiceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
