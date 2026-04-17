package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.screens.dashboard.StatusBadge
import com.nxzef.wc.shared.model.Lead
import org.koin.compose.viewmodel.koinViewModel

val PIPELINE_STAGES = listOf(
    "NEW", "CONTACTED", "NEGOTIATING", "WON", "LOST"
)

val STAGE_COLORS = mapOf(
    "NEW" to Color(0xFF2196F3),
    "CONTACTED" to Color(0xFFFF9800),
    "NEGOTIATING" to Color(0xFF9C27B0),
    "WON" to Color(0xFF4CAF50),
    "LOST" to Color(0xFFF44336)
)

@Composable
fun LeadPipelineScreen(
    onAddLead : () -> Unit,
    viewModel: LeadPipelineViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            state.error != null -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = {
                    viewModel.onAction(LeadPipelineAction.LoadLeads)
                }) { Text("Retry") }
            }

            else -> Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PIPELINE_STAGES.forEach { stage ->
                    KanbanColumn(
                        modifier = Modifier.weight(1f),
                        stage = stage,
                        leads = state.leads.filter {
                            it.status.name == stage
                        },
                        onLeadClick = { lead ->
                            viewModel.onAction(
                                LeadPipelineAction.SelectLead(lead)
                            )
                        }
                    )
                }
            }
        }
    }

    state.selectedLead?.let { lead ->
        LeadDetailDialog(
            lead = lead,
            onDismiss = {
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            },
            onUpdateStatus = { status, notes ->
                viewModel.onAction(
                    LeadPipelineAction.UpdateStatus(lead.id, status, notes)
                )
                viewModel.onAction(LeadPipelineAction.DismissDetail)
            }
        )
    }
}

@Composable
fun KanbanColumn(
    modifier: Modifier = Modifier,
    stage: String,
    leads: List<Lead>,
    onLeadClick: (Lead) -> Unit
) {
    val color = STAGE_COLORS[stage] ?: Color.Gray

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // Column header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color,
                modifier = Modifier.size(12.dp)
            ) {}
            Text(
                text = stage,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = color
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = leads.size.toString(),
                    fontSize = 11.sp,
                    color = color,
                    modifier = Modifier.padding(
                        horizontal = 8.dp, vertical = 2.dp
                    )
                )
            }
        }

        // Lead cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leads) { lead ->
                LeadCard(
                    lead = lead,
                    onClick = { onLeadClick(lead) }
                )
            }
        }
    }
}

@Composable
fun LeadCard(lead: Lead, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = lead.fullName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = lead.eventType.name,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            lead.eventDate?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = lead.phone,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Source chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = lead.source.name,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(
                        horizontal = 8.dp, vertical = 2.dp
                    )
                )
            }
        }
    }
}

@Composable
fun LeadDetailDialog(
    lead: Lead,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, String?) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(lead.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = lead.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                StatusBadge(status = lead.status.name)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("📱 Phone", lead.phone)
                lead.email?.let {
                    DetailRow("📧 Email", it)
                }
                DetailRow("🎉 Event", lead.eventType.name)
                lead.eventDate?.let {
                    DetailRow("📅 Date", it)
                }
                lead.location?.let {
                    DetailRow("📍 Location", it)
                }
                DetailRow("📣 Source", lead.source.name)

                HorizontalDivider()

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Status update
                Text(
                    text = "Update Status",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PIPELINE_STAGES.forEach { stage ->
                        if (stage != lead.status.name) {
                            val color = STAGE_COLORS[stage] ?: Color.Gray
                            OutlinedButton(
                                onClick = {
                                    onUpdateStatus(stage, notes.ifBlank { null })
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = color
                                ),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp
                                )
                            ) {
                                Text(text = stage, fontSize = 11.sp)
                            }
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
fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}