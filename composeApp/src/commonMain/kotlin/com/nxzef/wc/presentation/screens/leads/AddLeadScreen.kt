package com.nxzef.wc.presentation.screens.leads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.shared.model.EventType
import com.nxzef.wc.shared.model.LeadSource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadScreen(
    onLeadCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddLeadViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddLeadUiEvent.LeadCreated ->
                    onLeadCreated()

                is AddLeadUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add New Lead",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Capture a new enquiry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            HorizontalDivider()

            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Contact Info
                SectionHeader(
                    icon = Icons.Default.Person,
                    title = "Contact Information"
                )

                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = {
                        viewModel.onAction(AddLeadAction.OnFullNameChange(it))
                    },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, null)
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = {
                            viewModel.onAction(
                                AddLeadAction.OnPhoneChange(it)
                            )
                        },
                        label = { Text("Phone *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null)
                        }
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            viewModel.onAction(
                                AddLeadAction.OnEmailChange(it)
                            )
                        },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Email, null)
                        }
                    )
                }

                // Section: Event Details
                SectionHeader(
                    icon = Icons.Default.Event,
                    title = "Event Details"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Source dropdown
                    WCDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Lead Source",
                        selected = state.source.name,
                        options = LeadSource.entries.map { it.name },
                        onSelect = { name ->
                            viewModel.onAction(
                                AddLeadAction.OnSourceChange(
                                    LeadSource.valueOf(name)
                                )
                            )
                        }
                    )

                    // Event type dropdown
                    WCDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Event Type",
                        selected = state.eventType.name,
                        options = EventType.entries.map { it.name },
                        onSelect = { name ->
                            viewModel.onAction(
                                AddLeadAction.OnEventTypeChange(
                                    EventType.valueOf(name)
                                )
                            )
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.eventDate,
                        onValueChange = {
                            viewModel.onAction(
                                AddLeadAction.OnEventDateChange(it)
                            )
                        },
                        label = { Text("Event Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, null)
                        }
                    )
                    OutlinedTextField(
                        value = state.location,
                        onValueChange = {
                            viewModel.onAction(
                                AddLeadAction.OnLocationChange(it)
                            )
                        },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null)
                        }
                    )
                }

                // Section: Assignment
                SectionHeader(
                    icon = Icons.Default.AssignmentInd,
                    title = "Assignment"
                )

                if (state.isLoadingTeam) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    WCDropdown(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Assign To",
                        selected = state.teamMembers
                            .find { it.id == state.assignedTo }
                            ?.name ?: "Select team member",
                        options = state.teamMembers.map { it.name },
                        onSelect = { name ->
                            val user = state.teamMembers
                                .find { it.name == name }
                            user?.let {
                                viewModel.onAction(
                                    AddLeadAction.OnAssignedToChange(it.id)
                                )
                            }
                        }
                    )
                }

                // Notes
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        viewModel.onAction(AddLeadAction.OnNotesChange(it))
                    },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Notes, null)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit button
                Button(
                    onClick = {
                        viewModel.onAction(AddLeadAction.OnSubmit)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Lead",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WCDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}