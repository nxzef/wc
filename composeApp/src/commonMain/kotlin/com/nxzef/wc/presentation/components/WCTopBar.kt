package com.nxzef.wc.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.screens.notifications.NotificationAction
import com.nxzef.wc.presentation.screens.notifications.NotificationPanel
import com.nxzef.wc.presentation.screens.notifications.NotificationViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WCTopBar(
    title: String,
    subtitle: String? = null,
    notificationViewModel: NotificationViewModel = koinViewModel(),
    actions: @Composable () -> Unit = {}
) {
    val notifState by notificationViewModel.state.collectAsStateWithLifecycle()

    TopAppBar(
        title = {
            if (subtitle != null) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            actions()
            BadgedBox(
                badge = {
                    if (notifState.unreadCount > 0) {
                        Badge {
                            Text(
                                if (notifState.unreadCount > 99) "99+"
                                else notifState.unreadCount.toString()
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = {
                        notificationViewModel.onAction(
                            NotificationAction.Show
                        )
                    }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )

    // Notification panel overlay
    NotificationPanel(
        state = notifState,
        onAction = notificationViewModel::onAction
    )
}