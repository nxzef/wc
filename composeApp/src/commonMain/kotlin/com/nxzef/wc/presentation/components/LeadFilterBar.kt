package com.nxzef.wc.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.screens.leads.LeadPipelineAction
import com.nxzef.wc.presentation.screens.leads.LeadPipelineState
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeadFilterBar(
    state: LeadPipelineState,
    onAction: (LeadPipelineAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = remember { DateUtils.getCurrentYear() }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(LeadPipelineAction.OnSearchQueryChange(it)) },
                placeholder = { Text("Search leads...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = if (state.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onAction(LeadPipelineAction.OnSearchQueryChange("")) }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        }
                    }
                } else null,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Status Filter (God Chip)
                StatusMultiSelectChip(
                    allStatuses = state.statuses,
                    selectedStatusIds = state.filterStatusIds,
                    onSelectionChange = { onAction(LeadPipelineAction.OnFilterStatusesChange(it)) }
                )

                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Priority Filter
                FilterChipItem(
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
                    label = if (state.filterPriority == null) "Priority" else "${state.filterPriority}★",
                    icon = Icons.Default.Star
                )

                // Source Filter
                SourceFilterChip(
                    selectedSource = state.filterSource,
                    onSourceSelected = { onAction(LeadPipelineAction.OnFilterSourceChange(it)) }
                )

                // Month Filter
                MonthFilterChip(
                    selectedMonth = state.filterDateMonth,
                    onMonthSelected = { onAction(LeadPipelineAction.OnFilterMonthChange(it)) }
                )

                // Year Filter (at the right side of this scrollable row)
                YearFilterChip(
                    selectedYear = state.filterDateYear ?: currentYear,
                    onYearSelected = { onAction(LeadPipelineAction.OnFilterYearChange(it)) }
                )

                if (hasActiveFilters(state)) {
                    IconButton(
                        onClick = { onAction(LeadPipelineAction.ClearFilters) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterAltOff,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusMultiSelectChip(
    allStatuses: List<LeadStatus>,
    selectedStatusIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = if (selectedStatusIds.isEmpty() || selectedStatusIds.size == allStatuses.size) {
        "All Columns"
    } else {
        "${selectedStatusIds.size} Columns"
    }

    val isSelected = selectedStatusIds.isNotEmpty() && selectedStatusIds.size < allStatuses.size

    Box {
        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable { expanded = true },
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewColumn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp).background(MaterialTheme.colorScheme.surface)
        ) {
            val allSelected = selectedStatusIds.isEmpty() || selectedStatusIds.size == allStatuses.size
            
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = {
                                onSelectionChange(emptySet())
                                expanded = false
                            }
                        )
                        Text("All Columns", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                onClick = {
                    onSelectionChange(emptySet())
                    expanded = false
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            allStatuses.forEach { status ->
                val selected = selectedStatusIds.contains(status.id)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { isChecked ->
                                    val newSet = if (isChecked) {
                                        selectedStatusIds + status.id
                                    } else {
                                        selectedStatusIds - status.id
                                    }
                                    onSelectionChange(newSet)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.size(10.dp).background(status.color.toComposeColor(), MaterialTheme.shapes.extraSmall))
                            Spacer(Modifier.width(8.dp))
                            Text(status.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        val newSet = if (selected) {
                            selectedStatusIds - status.id
                        } else {
                            selectedStatusIds + status.id
                        }
                        onSelectionChange(newSet)
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChipItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
            enabled = true,
            selected = selected
        )
    )
}

@Composable
fun SourceFilterChip(
    selectedSource: LeadSource?,
    onSourceSelected: (LeadSource?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedSource != null,
            onClick = { expanded = true },
            label = { Text(selectedSource?.name ?: "Source", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(18.dp)) },
            shape = MaterialTheme.shapes.large
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All Sources") },
                onClick = { onSourceSelected(null); expanded = false }
            )
            LeadSource.entries.forEach { source ->
                DropdownMenuItem(
                    text = { Text(source.name) },
                    onClick = { onSourceSelected(source); expanded = false }
                )
            }
        }
    }
}

@Composable
fun MonthFilterChip(
    selectedMonth: Int?,
    onMonthSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedMonth != null,
            onClick = { expanded = true },
            label = { Text(if (selectedMonth == null) "Month" else DateUtils.getMonthName(selectedMonth), style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(18.dp)) },
            shape = MaterialTheme.shapes.large
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All Months") },
                onClick = { onMonthSelected(null); expanded = false }
            )
            (1..12).forEach { m ->
                DropdownMenuItem(
                    text = { Text(DateUtils.getMonthName(m)) },
                    onClick = { onMonthSelected(m); expanded = false }
                )
            }
        }
    }
}

@Composable
fun YearFilterChip(
    selectedYear: Int,
    onYearSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = remember { DateUtils.getCurrentYear() }
    val years = (currentYear - 2..currentYear + 2).toList()

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(selectedYear.toString(), style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(18.dp)) },
            shape = MaterialTheme.shapes.large
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = { onYearSelected(year); expanded = false }
                )
            }
        }
    }
}

private fun hasActiveFilters(state: LeadPipelineState): Boolean {
    return state.searchQuery.isNotEmpty() || 
           state.filterPriority != null || 
           state.filterSource != null || 
           state.filterDateMonth != null || 
           state.filterStatusIds.isNotEmpty() ||
           state.filterDateYear != null
}
