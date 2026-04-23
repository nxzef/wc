package com.nxzef.wc.presentation.screens.marketing

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.nxzef.wc.presentation.components.LeadStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingScreen(
    onBack: () -> Unit,
    onAddLead: () -> Unit,
    viewModel: MarketingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MarketingUiEvent.ShowSnackbar ->
                    snackbarState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Marketing",
                subtitle = "Welcome, ${state.userName}",
                onBack = onBack,
                actions = {
                    Button(
                        onClick = onAddLead,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Add Lead")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source stats
                    item {
                        Text(
                            text = "Lead Sources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.sourceStats.forEach { (source, count) ->
                                SourceStatCard(
                                    modifier = Modifier.weight(1f),
                                    source = source,
                                    count = count,
                                    total = state.leads.size
                                )
                            }
                        }
                    }

                    // Conversion summary
                    item {
                        val won = state.leads.count {
                            it.status == LeadStatus.WON
                        }
                        val lost = state.leads.count {
                            it.status == LeadStatus.LOST
                        }
                        val total = state.leads.size
                        val rate = if (total > 0)
                            (won * 100 / total) else 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme
                                    .colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceAround
                            ) {
                                ConversionStat(
                                    label = "Total",
                                    value = total.toString(),
                                    color = MaterialTheme.colorScheme
                                        .onPrimaryContainer
                                )
                                ConversionStat(
                                    label = "Won",
                                    value = won.toString(),
                                    color = WCTheme.colors.statusWon
                                )
                                ConversionStat(
                                    label = "Lost",
                                    value = lost.toString(),
                                    color = MaterialTheme.colorScheme.error
                                )
                                ConversionStat(
                                    label = "Rate",
                                    value = "$rate%",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Source filter chips
                    item {
                        Text(
                            text = "All Leads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.sourceFilter == null,
                                    onClick = {
                                        viewModel.onAction(
                                            MarketingAction.FilterBySource(null)
                                        )
                                    },
                                    label = { Text("All") }
                                )
                            }
                            items(LeadSource.entries) { source ->
                                FilterChip(
                                    selected = state.sourceFilter == source,
                                    onClick = {
                                        viewModel.onAction(
                                            MarketingAction.FilterBySource(
                                                source
                                            )
                                        )
                                    },
                                    label = { Text(source.name) }
                                )
                            }
                        }
                    }

                    // Leads list
                    if (viewModel.filteredLeads.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No leads found",
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(viewModel.filteredLeads) { lead ->
                            MarketingLeadCard(lead = lead)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceStatCard(
    modifier: Modifier = Modifier,
    source: LeadSource,
    count: Int,
    total: Int
) {
    val color = when (source) {
        LeadSource.INSTAGRAM -> WCTheme.colors.sourceInstagram
        LeadSource.FACEBOOK -> WCTheme.colors.sourceFacebook
        LeadSource.GOOGLE -> WCTheme.colors.sourceGoogle
        LeadSource.REFERRAL -> WCTheme.colors.sourceReferral
        LeadSource.WALK_IN -> WCTheme.colors.sourceWalkIn
        LeadSource.OTHER -> WCTheme.colors.sourceOther
    }
    val pct = if (total > 0) (count * 100 / total) else 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = source.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConversionStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MarketingLeadCard(lead: Lead) {
    val sourceColor = when (lead.source) {
        LeadSource.INSTAGRAM -> WCTheme.colors.sourceInstagram
        LeadSource.FACEBOOK -> WCTheme.colors.sourceFacebook
        LeadSource.GOOGLE -> WCTheme.colors.sourceGoogle
        LeadSource.REFERRAL -> WCTheme.colors.sourceReferral
        LeadSource.WALK_IN -> WCTheme.colors.sourceWalkIn
        LeadSource.OTHER -> WCTheme.colors.sourceOther
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp),
                color = sourceColor
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${lead.eventType.name} • ${lead.source.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                lead.eventDate?.let {
                    Text(
                        text = "📅 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LeadStatusBadge(status = lead.status.name)
        }
    }
}