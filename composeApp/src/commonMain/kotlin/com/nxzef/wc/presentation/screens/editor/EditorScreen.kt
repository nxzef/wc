package com.nxzef.wc.presentation.screens.editor

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.BookingStatusBadge
import com.nxzef.wc.presentation.components.TaskCheckItem
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.shared.model.UserRole
import com.nxzef.wc.presentation.screens.photographer.ShootCard
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.BookingStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    val isMainScreen = remember { SessionManager.getRole() == UserRole.EDITOR }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is EditorUiEvent.ShowSnackbar ->
                    snackbarState.showSnackbar(event.message)

                EditorUiEvent.StatusUpdated ->
                    snackbarState.showSnackbar("Marked as delivered!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Editing Queue",
                subtitle = "Welcome, ${state.userName}",
                onBack = if (isMainScreen) null else onBack,
                showNotificationIcon = isMainScreen
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.onAction(EditorAction.Load) },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Retry")
                        }
                    }
                }

                state.queue.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "No editing jobs assigned",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1000.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val priority = state.queue.filter { it.status == BookingStatus.SHOOT_DONE }
                        val inProgress = state.queue.filter { it.status == BookingStatus.EDITING }
                        val rest = state.queue.filter { it.status != BookingStatus.SHOOT_DONE && it.status != BookingStatus.EDITING }

                        if (priority.isNotEmpty()) {
                            item {
                                SectionHeader("Ready for Editing", MaterialTheme.colorScheme.error)
                            }
                            items(priority) { job ->
                                ShootCard(job, onClick = { viewModel.onAction(EditorAction.SelectJob(job)) })
                            }
                        }

                        if (inProgress.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                SectionHeader("In Progress", WCTheme.colors.statusEditing)
                            }
                            items(inProgress) { job ->
                                ShootCard(job, onClick = { viewModel.onAction(EditorAction.SelectJob(job)) })
                            }
                        }

                        if (rest.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                SectionHeader("Others", MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                            items(rest) { job ->
                                ShootCard(job, onClick = { viewModel.onAction(EditorAction.SelectJob(job)) })
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedJob?.let { job ->
        AlertDialog(
            onDismissRequest = { viewModel.onAction(EditorAction.DismissDetail) },
            shape = MaterialTheme.shapes.large,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = job.eventType, fontWeight = FontWeight.Bold)
                    BookingStatusBadge(status = job.status)
                }
            },
            text = {
                Column(
                    modifier = Modifier.widthIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📍 ${job.location}", style = MaterialTheme.typography.bodyLarge)
                        Text("📅 ${job.eventDate}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }

                    if (state.tasks.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "Editing Tasks",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.tasks.forEach { task ->
                                TaskCheckItem(
                                    task = task,
                                    onToggle = { viewModel.onAction(EditorAction.MarkTaskDone(task.id, it)) }
                                )
                            }
                        }
                    }

                    if (job.status == BookingStatus.SHOOT_DONE || job.status == BookingStatus.EDITING) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.onAction(EditorAction.MarkEditingDone(job.id)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = WCTheme.colors.statusDelivered)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mark as Delivered")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(EditorAction.DismissDetail) }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = color,
            modifier = Modifier.size(width = 4.dp, height = 20.dp)
        ) {}
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
