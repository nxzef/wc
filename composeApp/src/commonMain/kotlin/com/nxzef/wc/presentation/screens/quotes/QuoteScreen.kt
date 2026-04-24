package com.nxzef.wc.presentation.screens.quotes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.components.QuoteStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.CreateQuoteItemRequest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    leadId: String,
    onBack: () -> Unit,
    viewModel: QuoteViewModel = koinViewModel()
) {
    val state by viewModel.state
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(leadId) {
        viewModel.onAction(QuoteContract.Action.LoadQuotes(leadId))
    }

    Scaffold(
        topBar = {
            WCTopBar(
                title = "Quotes",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Quote")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 800.dp),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.quotes) { quote ->
                        QuoteItemCard(
                            quote = quote,
                            onStatusUpdate = { status ->
                                viewModel.onAction(QuoteContract.Action.UpdateStatus(quote.id, status))
                            }
                        )
                    }
                }
            }

            if (showCreateDialog) {
                CreateQuoteDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { notes, items ->
                        viewModel.onAction(QuoteContract.Action.CreateQuote(notes, items))
                        showCreateDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun QuoteItemCard(
    quote: Quote,
    onStatusUpdate: (QuoteStatus) -> Unit
) {
    Card(
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
                Text(
                    text = "Quote #${quote.id.takeLast(4)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                QuoteStatusBadge(status = quote.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            quote.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${quote.totalAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (quote.notes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "Notes: ${quote.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            if (quote.status == QuoteStatus.SENT || quote.status == QuoteStatus.DRAFT) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = { onStatusUpdate(QuoteStatus.REJECTED) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reject")
                    }
                    Button(onClick = { onStatusUpdate(QuoteStatus.ACCEPTED) }) {
                        Text("Accept Quote")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<CreateQuoteItemRequest>) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<CreateQuoteItemRequest>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Create Quote", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                Text("Items", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemDescription,
                        onValueChange = { itemDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price") },
                        modifier = Modifier.width(80.dp)
                    )
                }
                Button(
                    onClick = {
                        val price = itemPrice.toDoubleOrNull() ?: 0.0
                        if (itemDescription.isNotBlank()) {
                            items.add(CreateQuoteItemRequest(itemDescription, price))
                            itemDescription = ""
                            itemPrice = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Item")
                }
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(items) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.description)
                            Text("$${item.price}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(notes, items.toList()) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
