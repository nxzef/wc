package com.nxzef.wc.presentation.screens.team

import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.screens.leads.WCDropdown
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    onBack: () -> Unit,
    viewModel: TeamViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TeamUiEvent.ShowSnackbar ->
                    snackbarState.showSnackbar(event.message)

                is TeamUiEvent.MemberCreated ->
                    snackbarState.showSnackbar("Team member added!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Team Management",
                subtitle = "${state.team.size} members",
                onBack = onBack,
                actions = {
                    Button(
                        onClick = {
                            viewModel.onAction(TeamAction.ShowAddDialog)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Add Member")
                    }
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
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.team.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No team members yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add your first team member",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 800.dp),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Group by role
                        UserRole.entries.forEach { role ->
                            val members = state.team.filter {
                                it.role == role
                            }
                            if (members.isNotEmpty()) {
                                item {
                                    Text(
                                        text = role.name
                                            .replace("_", " "),
                                        style = MaterialTheme.typography
                                            .labelLarge,
                                        color = MaterialTheme.colorScheme
                                            .primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                    )
                                }
                                items(members) { member ->
                                    TeamMemberCard(member = member)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add member dialog
    if (state.showAddDialog) {
        AddMemberDialog(
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun TeamMemberCard(member: User) {
    val roleColor = when (member.role) {
        UserRole.OWNER ->
            MaterialTheme.colorScheme.primary

        UserRole.LEAD_MANAGER ->
            MaterialTheme.colorScheme.tertiary

        UserRole.MARKETING ->
            MaterialTheme.colorScheme.secondary

        UserRole.PHOTOGRAPHER ->
            MaterialTheme.colorScheme.error

        UserRole.EDITOR ->
            MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Surface(
                shape = RoundedCornerShape(50),
                color = roleColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = member.name.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Role badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = roleColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = member.role.name
                        .replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    )
                )
            }

            // Active indicator
            Surface(
                shape = RoundedCornerShape(50),
                color = if (member.isActive)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(10.dp)
            ) {}
        }
    }
}

@Composable
fun AddMemberDialog(
    state: TeamState,
    onAction: (TeamAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onAction(TeamAction.HideAddDialog) },
        title = {
            Text(
                text = "Add Team Member",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.newName,
                    onValueChange = {
                        onAction(TeamAction.OnNameChange(it))
                    },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, null)
                    }
                )

                OutlinedTextField(
                    value = state.newEmail,
                    onValueChange = {
                        onAction(TeamAction.OnEmailChange(it))
                    },
                    label = { Text("Email *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Email, null)
                    }
                )

                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = {
                        onAction(TeamAction.OnPasswordChange(it))
                    },
                    label = { Text("Password *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    }
                )

                WCDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Role",
                    selected = state.newRole.name
                        .replace("_", " "),
                    options = UserRole.entries
                        .filter { it != UserRole.OWNER }
                        .map { it.name.replace("_", " ") },
                    onSelect = { name ->
                        onAction(
                            TeamAction.OnRoleChange(
                                UserRole.valueOf(
                                    name.replace(" ", "_")
                                )
                            )
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAction(TeamAction.OnCreateMember) },
                enabled = !state.isCreating
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Member")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(TeamAction.HideAddDialog) }
            ) {
                Text("Cancel")
            }
        }
    )
}