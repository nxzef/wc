package com.nxzef.wc.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val user = viewModel.user
    val serverConnected by viewModel.serverConnected.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.message.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { WCTopBar(title = "Settings", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ── Profile hero ─────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.name?.first()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = user?.name ?: "Unknown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = user?.role?.name?.replace("_", " ") ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Account ───────────────────────────────────────────────────
                SettingsSectionLabel("Account")
                Spacer(Modifier.height(6.dp))
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFFFF9800),
                        title = "Change Password",
                        subtitle = "Update your security credentials",
                        showArrow = true,
                        onClick = { showChangePasswordDialog = true }
                    )
                }

                // ── Role-specific ─────────────────────────────────────────────
                user?.role?.let { role ->
                    val sectionData = when (role) {
                        UserRole.OWNER -> RoleSection(
                            "Company",
                            listOf(
                                RoleItem("Company Name", "The Wedding Clouds", true),
                                RoleItem("Export Data", "Coming soon", false),
                                RoleItem("Backup", "Coming soon", false)
                            )
                        )
                        UserRole.LEAD_MANAGER -> RoleSection(
                            "Notifications",
                            listOf(
                                RoleItem("Lead assigned to me", "Enabled", false),
                                RoleItem("Quote accepted", "Enabled", false)
                            )
                        )
                        UserRole.PHOTOGRAPHER -> RoleSection(
                            "My Work",
                            listOf(
                                RoleItem("Portfolio Link", "Not set", false),
                                RoleItem("Preferred shoot types", "None", false)
                            )
                        )
                        UserRole.EDITOR -> RoleSection(
                            "My Work",
                            listOf(
                                RoleItem("Editing style notes", "Default", false),
                                RoleItem("Delivery format preferences", "JPEG", false)
                            )
                        )
                        UserRole.MARKETING -> RoleSection(
                            "Analytics",
                            listOf(RoleItem("My leads count", "0", true))
                        )
                    }

                    val roleIcon = when (role) {
                        UserRole.OWNER        -> Icons.Default.Business
                        UserRole.LEAD_MANAGER -> Icons.Default.Notifications
                        UserRole.PHOTOGRAPHER -> Icons.Default.PhotoCamera
                        UserRole.EDITOR       -> Icons.Default.Edit
                        UserRole.MARKETING    -> Icons.Default.BarChart
                    }
                    val roleIconColor = when (role) {
                        UserRole.OWNER        -> Color(0xFF1E88E5)
                        UserRole.LEAD_MANAGER -> Color(0xFFE53935)
                        UserRole.PHOTOGRAPHER -> Color(0xFF8E24AA)
                        UserRole.EDITOR       -> Color(0xFF00ACC1)
                        UserRole.MARKETING    -> Color(0xFF00897B)
                    }

                    Spacer(Modifier.height(24.dp))
                    SettingsSectionLabel(sectionData.title)
                    Spacer(Modifier.height(6.dp))
                    SettingsGroup {
                        sectionData.items.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(
                                modifier = Modifier.padding(start = 74.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            SettingsRow(
                                icon = roleIcon,
                                iconColor = roleIconColor,
                                title = item.title,
                                value = if (item.isActuallyAvailable || item.value != "Coming soon") item.value else null,
                                comingSoon = !item.isActuallyAvailable && item.value == "Coming soon",
                                enabled = item.isActuallyAvailable
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── General ───────────────────────────────────────────────────
                SettingsSectionLabel("General")
                Spacer(Modifier.height(6.dp))
                val serverColor = if (serverConnected) Color(0xFF43A047) else MaterialTheme.colorScheme.error
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        title = "App Version",
                        value = "1.0.0 MVP"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 74.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    SettingsRow(
                        icon = Icons.Default.Cloud,
                        iconColor = serverColor,
                        title = "Server Status",
                        value = if (serverConnected) "Connected" else "Disconnected",
                        valueColor = serverColor
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 74.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        iconColor = Color(0xFF7E57C2),
                        title = "Dark Mode",
                        comingSoon = true,
                        enabled = false
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                viewModel.changePassword(current, new)
                showChangePasswordDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    valueColor: Color? = null,
    showArrow: Boolean = false,
    comingSoon: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled)
                    Modifier.clickable { onClick() }
                else
                    Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = iconColor.copy(alpha = if (enabled) 0.14f else 0.07f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) iconColor else iconColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
        }

        when {
            comingSoon -> Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = Color(0xFFFF9800).copy(alpha = 0.14f)
            ) {
                Text(
                    text = "SOON",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
            value != null -> Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
            showArrow -> Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class RoleSection(val title: String, val items: List<RoleItem>)
data class RoleItem(val title: String, val value: String, val isActuallyAvailable: Boolean)

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = newPassword != confirmPassword && confirmPassword.isNotEmpty()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onConfirm(currentPassword, newPassword) },
                        enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}
