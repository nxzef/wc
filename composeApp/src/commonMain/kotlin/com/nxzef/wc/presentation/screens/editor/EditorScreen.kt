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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriorityHigh
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
import com.nxzef.wc.presentation.components.BookingStatusBadge
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.presentation.screens.photographer.ShootCard
import com.nxzef.wc.presentation.screens.photographer.TaskCheckItem
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
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                state.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = {
                                viewModel.onAction(EditorAction.Load)
                            }) { Text("Retry") }
                        }
                    }
                }

                state.queue.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                            Text(
                                text = "No editing jobs assigned",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Priority — SHOOT_DONE first
                        val priority = state.queue.filter {
                            it.status == BookingStatus.SHOOT_DONE
                        }
                        val inProgress = state.queue.filter {
                            it.status == BookingStatus.EDITING
                        }
                        val rest = state.queue.filter {
                            it.status != BookingStatus.SHOOT_DONE &&
                                    it.status != BookingStatus.EDITING
                        }

                        if (priority.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(width = 4.dp, height = 16.dp)
                                    ) {}
                                    Text(
                                        text = "Ready for Editing",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            items(priority) { job ->
                                ShootCard(
                                    shoot = job,
                                    onClick = {
                                        viewModel.onAction(
                                            EditorAction.SelectJob(job)
                                        )
                                    }
                                )
                            }
                        }

                        if (inProgress.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = WCTheme.colors.statusEditing,
                                        modifier = Modifier.size(width = 4.dp, height = 16.dp)
                                    ) {}
                                    Text(
                                        text = "In Progress",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WCTheme.colors.statusEditing
                                    )
                                }
                            }
                            items(inProgress) { job ->
                                ShootCard(
                                    shoot = job,
                                    onClick = {
                                        viewModel.onAction(
                                            EditorAction.SelectJob(job)
                                        )
                                    }
                                )
                            }
                        }

                        if (rest.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(width = 4.dp, height = 16.dp)
                                    ) {}
                                    Text(
                                        text = "Others",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(rest) { job ->
                                ShootCard(
                                    shoot = job,
                                    onClick = {
                                        viewModel.onAction(
                                            EditorAction.SelectJob(job)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedJob?.let { job ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onAction(EditorAction.DismissDetail)
            },
            title = {
                Text(
                    text = job.eventType,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("📍 ${job.location}")
                        Text("📅 ${job.eventDate}")
                        BookingStatusBadge(status = job.status)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (state.tasks.isNotEmpty()) {
                        item {
                            HorizontalDivider()
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography
                                    .labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                            )
                        }
                        items(state.tasks) { task ->
                            TaskCheckItem(
                                task = task,
                                onToggle = { done ->
                                    viewModel.onAction(
                                        EditorAction.MarkTaskDone(
                                            task.id, done
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (job.status == BookingStatus.SHOOT_DONE ||
                        job.status == BookingStatus.EDITING
                    ) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.onAction(
                                        EditorAction.MarkEditingDone(job.id)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WCTheme.colors.statusDelivered
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Mark as Delivered")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onAction(EditorAction.DismissDetail)
                }) { Text("Close") }
            }
        )
    }
}