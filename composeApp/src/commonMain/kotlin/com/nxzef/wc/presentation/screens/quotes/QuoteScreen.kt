package com.nxzef.wc.presentation.screens.quotes

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nxzef.wc.platform.pickPdfFile
import com.nxzef.wc.presentation.components.QuoteStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.shared.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuoteScreen(
    leadId: String,
    clientName: String,
    clientEmail: String,
    onBack: () -> Unit,
    viewModel: QuoteViewModel = koinViewModel()
) {
    val state by viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedQuote by remember { mutableStateOf<Quote?>(null) }

    LaunchedEffect(leadId) {
        viewModel.onAction(QuoteContract.Action.LoadQuotes(leadId, clientName, clientEmail))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is QuoteContract.UiEvent.QuoteSent ->
                    snackbarHostState.showSnackbar("Quote sent to ${event.email}")

                is QuoteContract.UiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { WCTopBar(title = "Quotes", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Client info header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = clientName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = clientEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Send quote section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Send New Quote",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedButton(
                                onClick = {
                                    val result = pickPdfFile()
                                    if (result != null) {
                                        viewModel.onAction(
                                            QuoteContract.Action.AttachPdf(
                                                result.first,
                                                result.second,
                                                result.third
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Attach Quote PDF")
                            }

                            if (state.selectedFileName != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Selected: ${state.selectedFileName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = state.amountInput,
                                onValueChange = {
                                    viewModel.onAction(QuoteContract.Action.OnAmountChange(it))
                                },
                                label = { Text("Quote Amount (₹) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )

                            OutlinedTextField(
                                value = state.notesInput,
                                onValueChange = {
                                    viewModel.onAction(QuoteContract.Action.OnNotesChange(it))
                                },
                                label = { Text("Add a note (optional)") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )

                            val amountValid =
                                state.amountInput.toDoubleOrNull()?.let { it > 0.0 } == true

                            Button(
                                onClick = { viewModel.onAction(QuoteContract.Action.SendQuote) },
                                enabled = state.selectedFileName != null && !state.isSending,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                if (state.isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sending...")
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Send Quote")
                                }
                            }
                        }
                    }
                }

                // History section header
                if (state.quotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Quote History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(state.quotes) { quote ->
                        QuoteHistoryCard(
                            quote = quote,
                            onClick = { selectedQuote = quote },
                            onStatusUpdate = { status ->
                                viewModel.onAction(
                                    QuoteContract.Action.UpdateStatus(quote.id, status)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    selectedQuote?.let { quote ->
        QuoteDetailDialog(
            quote = quote,
            onDismiss = { selectedQuote = null },
            onStatusUpdate = { status ->
                viewModel.onAction(QuoteContract.Action.UpdateStatus(quote.id, status))
                selectedQuote = null
            }
        )
    }
}

@Composable
fun QuoteHistoryCard(
    quote: Quote,
    onClick: () -> Unit,
    onStatusUpdate: (QuoteStatus) -> Unit
) {
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.fileName ?: "Quote #${quote.id.takeLast(4)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyUtils.formatINR(quote.totalAmount)} · ${DateUtils.formatDisplayDate(quote.createdAt.take(10))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                QuoteStatusBadge(status = quote.status)
            }

            if (quote.status == QuoteStatus.SENT) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = { onStatusUpdate(QuoteStatus.REJECTED) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Rejected")
                    }
                    Button(
                        onClick = { onStatusUpdate(QuoteStatus.ACCEPTED) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Accepted")
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteDetailDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    onStatusUpdate: (QuoteStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = quote.fileName ?: "Quote #${quote.id.takeLast(4)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatDisplayDate(quote.createdAt.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QuoteStatusBadge(status = quote.status)
                }

                HorizontalDivider()

                Text(
                    text = CurrencyUtils.formatINR(quote.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val quoteNotes = quote.notes
                if (!quoteNotes.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = quoteNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (quote.status == QuoteStatus.SENT) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onStatusUpdate(QuoteStatus.REJECTED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Mark Rejected")
                        }
                        Button(
                            onClick = { onStatusUpdate(QuoteStatus.ACCEPTED) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Mark Accepted")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
