package com.nxzef.wc.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nxzef.wc.presentation.screens.leads.LeadPipelineAction
import com.nxzef.wc.presentation.screens.leads.LeadPipelineState
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.LeadStatus
import com.nxzef.wc.shared.util.DateUtils

private val StarGold = Color(0xFFFFC107)

@Composable
fun LeadFilterBar(
    state: LeadPipelineState,
    onAction: (LeadPipelineAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = remember { DateUtils.getCurrentYear() }

    // Derive available years from actual lead event dates
    val availableYears = remember(state.leads) {
        val years = state.leads.mapNotNull { DateUtils.getYear(it.eventDate) }.toSortedSet()
        if (years.isEmpty()) listOf(currentYear) else years.toList()
    }

    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Search (start) ───────────────────────────────────────────────────
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(LeadPipelineAction.OnSearchQueryChange(it)) },
            placeholder = {
                Text("Search leads…", style = MaterialTheme.typography.bodySmall)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = if (state.searchQuery.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onAction(LeadPipelineAction.OnSearchQueryChange("")) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(15.dp))
                    }
                }
            } else null,
            modifier = Modifier.width(220.dp).height(48.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )

        // ── Chips (centred in remaining middle space) ─────────────────────
        Box(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // God chip — status column selector
                StatusMultiSelectChip(
                    allStatuses = state.statuses,
                    selectedStatusIds = state.filterStatusIds,
                    onSelectionChange = { onAction(LeadPipelineAction.OnFilterStatusesChange(it)) }
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Priority — gold star, 5 levels
                PriorityFilterChip(
                    filterPriority = state.filterPriority,
                    onPriorityChange = { onAction(LeadPipelineAction.OnFilterPriorityChange(it)) }
                )

                // Source
                SourceFilterChip(
                    selectedSource = state.filterSource,
                    onSourceSelected = { onAction(LeadPipelineAction.OnFilterSourceChange(it)) }
                )

                // Event Type
                EventTypeFilterChip(
                    selectedEventType = state.filterEventType,
                    onEventTypeSelected = { onAction(LeadPipelineAction.OnFilterEventTypeChange(it)) }
                )

                // Month
                MonthFilterChip(
                    selectedMonth = state.filterDateMonth,
                    onMonthSelected = { onAction(LeadPipelineAction.OnFilterMonthChange(it)) }
                )

                // Clear all filters
                if (hasActiveFilters(state)) {
                    IconButton(
                        onClick = { onAction(LeadPipelineAction.ClearFilters) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterAltOff,
                            contentDescription = "Clear All Filters",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ── Year (end, pinned) ───────────────────────────────────────────────
        YearFilterChip(
            selectedYear = state.filterDateYear,
            availableYears = availableYears,
            onYearSelected = { onAction(LeadPipelineAction.OnFilterYearChange(it)) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status "God Chip"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatusMultiSelectChip(
    allStatuses: List<LeadStatus>,
    selectedStatusIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isFiltered = selectedStatusIds.isNotEmpty() && selectedStatusIds.size < allStatuses.size

    val label = when {
        isFiltered -> "(${selectedStatusIds.size}) selected"
        else -> "All Columns"
    }

    Box {
        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable { expanded = true },
            color = if (isFiltered)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(
                1.5.dp,
                if (isFiltered) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
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
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp).background(MaterialTheme.colorScheme.surface)
        ) {
            val allSelected =
                selectedStatusIds.isEmpty() || selectedStatusIds.size == allStatuses.size

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
                                    onSelectionChange(
                                        if (isChecked) selectedStatusIds + status.id
                                        else selectedStatusIds - status.id
                                    )
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        status.color.toComposeColor(),
                                        MaterialTheme.shapes.extraSmall
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(status.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        onSelectionChange(
                            if (selected) selectedStatusIds - status.id
                            else selectedStatusIds + status.id
                        )
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Priority chip — gold star, 5 levels
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PriorityFilterChip(
    filterPriority: Int?,
    onPriorityChange: (Int?) -> Unit
) {
    val isActive = filterPriority != null
    val label = if (filterPriority == null) "Priority" else "${filterPriority}★"

    FilterChip(
        selected = isActive,
        onClick = {
            val next = when (filterPriority) {
                null -> 5; 5 -> 4; 4 -> 3; 3 -> 2; 2 -> 1; else -> null
            }
            onPriorityChange(next)
        },
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                Icons.Default.Star, null,
                modifier = Modifier.size(16.dp),
                tint = StarGold
            )
        },
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StarGold.copy(alpha = 0.12f),
            selectedLabelColor = StarGold,
            selectedLeadingIconColor = StarGold
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isActive,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = StarGold,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Source chip
// ─────────────────────────────────────────────────────────────────────────────

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
            label = {
                Text(
                    selectedSource?.name ?: "Source",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
            },
            shape = MaterialTheme.shapes.large,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
            )
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

// ─────────────────────────────────────────────────────────────────────────────
// Event type chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EventTypeFilterChip(
    selectedEventType: EventType?,
    onEventTypeSelected: (EventType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedEventType != null,
            onClick = { expanded = true },
            label = {
                Text(
                    selectedEventType?.name ?: "Event",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
            },
            shape = MaterialTheme.shapes.large,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All Events") },
                onClick = { onEventTypeSelected(null); expanded = false }
            )
            EventType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = { onEventTypeSelected(type); expanded = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Month chip
// ─────────────────────────────────────────────────────────────────────────────

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
            label = {
                Text(
                    if (selectedMonth == null) "Month" else DateUtils.getMonthName(selectedMonth),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
            },
            shape = MaterialTheme.shapes.large,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
            )
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

// ─────────────────────────────────────────────────────────────────────────────
// Year chip — only shows years present in leads
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun YearFilterChip(
    selectedYear: Int?,
    availableYears: List<Int>,
    onYearSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = selectedYear != null
    Box {
        FilterChip(
            selected = isActive,
            onClick = { expanded = true },
            label = {
                Text(
                    selectedYear?.toString() ?: "Year",
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
            },
            shape = MaterialTheme.shapes.large,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (isActive) {
                DropdownMenuItem(
                    text = { Text("All Years") },
                    onClick = { onYearSelected(null); expanded = false }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            }
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            year.toString(),
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onYearSelected(year); expanded = false }
                )
            }
        }
    }
}

internal fun hasActiveFilters(state: LeadPipelineState): Boolean =
    state.searchQuery.isNotEmpty() ||
    state.filterPriority != null ||
    state.filterSource != null ||
    state.filterEventType != null ||
    state.filterDateMonth != null ||
    state.filterStatusIds.isNotEmpty() ||
    state.filterDateYear != null
