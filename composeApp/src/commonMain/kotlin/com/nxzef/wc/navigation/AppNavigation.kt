package com.nxzef.wc.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nxzef.wc.data.model.UserRole
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.auth.LoginScreen

sealed class Screen {
    object Login : Screen()
    object OwnerDashboard : Screen()
    object LeadPipeline : Screen()
    object MarketingLeads : Screen()
    object MyShots : Screen()
    object EditingQueue : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember {
        mutableStateOf<Screen>(Screen.Login)
    }

    when (currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = { role ->
                    currentScreen = when (role) {
                        UserRole.OWNER        -> Screen.OwnerDashboard
                        UserRole.LEAD_MANAGER -> Screen.LeadPipeline
                        UserRole.MARKETING    -> Screen.MarketingLeads
                        UserRole.PHOTOGRAPHER -> Screen.MyShots
                        UserRole.EDITOR       -> Screen.EditingQueue
                    }
                }
            )
        }

        is Screen.OwnerDashboard -> {
            PlaceholderScreen(
                title = "👑 Owner Dashboard",
                subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                onLogout = { currentScreen = Screen.Login }
            )
        }

        is Screen.LeadPipeline -> {
            PlaceholderScreen(
                title = "🔥 Lead Pipeline",
                subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                onLogout = { currentScreen = Screen.Login }
            )
        }

        is Screen.MarketingLeads -> {
            PlaceholderScreen(
                title = "📣 Marketing",
                subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                onLogout = { currentScreen = Screen.Login }
            )
        }

        is Screen.MyShots -> {
            PlaceholderScreen(
                title = "📸 My Shoots",
                subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                onLogout = { currentScreen = Screen.Login }
            )
        }

        is Screen.EditingQueue -> {
            PlaceholderScreen(
                title = "🎨 Editing Queue",
                subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                onLogout = { currentScreen = Screen.Login }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, fontSize = 32.sp)
            Text(text = subtitle, fontSize = 18.sp)
            Text(
                text = "Role: ${SessionManager.currentUser?.role}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
            Button(onClick = {
                SessionManager.clear()
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}