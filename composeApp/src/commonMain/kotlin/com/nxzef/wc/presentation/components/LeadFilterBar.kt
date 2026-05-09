package com.nxzef.wc.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.screens.leads.LeadPipelineAction
import com.nxzef.wc.presentation.screens.leads.LeadPipelineState
import com.nxzef.wc.shared.model.LeadSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadFilterBar(
    state: LeadPipelineState,
    onAction: (LeadPipelineAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSourceMenu by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Field
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(LeadPipelineAction.OnSearchQueryChange(it)) },
            placeholder = { Text("Search name, phone...") },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.width(200.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )

        // Priority Filter
        FilterChip(
            selected = state.filterPriority != null,
            onClick = {
                val next = when (state.filterPriority) {
                    null -> 3
                    3 -> 2
                    2 -> 1
                    else -> null
                }
                onAction(LeadPipelineAction.OnFilterPriorityChange(next))
            },
            label = {
                Text(
                    if (state.filterPriority == null) "All Priorities"
                    else "Priority: ${state.filterPriority}★"
                )
            },
            leadingIcon = { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) }
        )

        // Source Filter
        Box {
            FilterChip(
                selected = state.filterSource != null,
                onClick = { showSourceMenu = true },
                label = { Text(state.filterSource?.name ?: "All Sources") },
                leadingIcon = { Icon(Icons.Default.Source, null, modifier = Modifier.size(16.dp)) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
            )
            DropdownMenu(expanded = showSourceMenu, onDismissRequest = { showSourceMenu = false }) {
                DropdownMenuItem(
                    text = { Text("All Sources") },
                    onClick = { onAction(LeadPipelineAction.OnFilterSourceChange(null)); showSourceMenu = false }
                )
                LeadSource.entries.forEach { source ->
                    DropdownMenuItem(
                        text = { Text(source.name) },
                        onClick = { onAction(LeadPipelineAction.OnFilterSourceChange(source)); showSourceMenu = false }
                    )
                }
            }
        }

        // Month Filter
        Box {
            FilterChip(
                selected = state.filterDateMonth != null,
                onClick = { showMonthMenu = true },
                label = { Text(if (state.filterDateMonth == null) "All Months" else monthName(state.filterDateMonth)) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp)) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
            )
            DropdownMenu(expanded = showMonthMenu, onDismissRequest = { showMonthMenu = false }) {
                DropdownMenuItem(
                    text = { Text("All Months") },
                    onClick = { onAction(LeadPipelineAction.OnFilterMonthChange(null)); showMonthMenu = false }
                )
                (1..12).forEach { m ->
                    DropdownMenuItem(
                        text = { Text(monthName(m)) },
                        onClick = { onAction(LeadPipelineAction.OnFilterMonthChange(m)); showMonthMenu = false }
                    )
                }
            }
        }

        if (state.searchQuery.isNotEmpty() || state.filterPriority != null || state.filterSource != null || state.filterDateMonth != null) {
            TextButton(onClick = { onAction(LeadPipelineAction.ClearFilters) }) {
                Icon(Icons.Default.ClearAll, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }
        }
    }
}

private fun monthName(m: Int): String = when (m) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
    else -> ""
}
