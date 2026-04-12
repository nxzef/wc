package com.nxzef.wc.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nxzef.wc.data.model.UserRole
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.screens.auth.LoginScreen
import com.nxzef.wc.presentation.theme.WCTheme

@Composable
fun WCNavigation() {
    val navController = rememberNavController()

    WCTheme {
        Surface {
            NavHost(
                navController = navController,
                startDestination = Route.Login,

                ) {
                // Login Screen
                composable<Route.Login> {
                    LoginScreen(
                        onLoginSuccess = { role ->
                            val destination: Route = when (role) {
                                UserRole.OWNER -> Route.OwnerDashboard
                                UserRole.LEAD_MANAGER -> Route.LeadPipeline
                                UserRole.MARKETING -> Route.Marketing
                                UserRole.PHOTOGRAPHER -> Route.MyShoots
                                UserRole.EDITOR -> Route.EditingQueue
                            }

                            navController.navigate(destination) {
                                // Pop up to Login to remove it from history
                                popUpTo<Route.Login> { inclusive = true }
                            }
                        }
                    )
                }

                // Owner Dashboard
                composable<Route.OwnerDashboard> {
                    PlaceholderScreen(
                        title = "👑 Owner Dashboard",
                        subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                        role = SessionManager.currentUser?.role?.name ?: "",
                        onLogout = { handleLogout(navController) }
                    )
                }

                // Lead Pipeline
                composable<Route.LeadPipeline> {
                    PlaceholderScreen(
                        title = "🔥 Lead Pipeline",
                        subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                        role = SessionManager.currentUser?.role?.name ?: "",
                        onLogout = { handleLogout(navController) }
                    )
                }

                // Marketing
                composable<Route.Marketing> {
                    PlaceholderScreen(
                        title = "📣 Marketing",
                        subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                        role = SessionManager.currentUser?.role?.name ?: "",
                        onLogout = { handleLogout(navController) }
                    )
                }

                // My Shoots
                composable<Route.MyShoots> {
                    PlaceholderScreen(
                        title = "📸 My Shoots",
                        subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                        role = SessionManager.currentUser?.role?.name ?: "",
                        onLogout = { handleLogout(navController) }
                    )
                }

                // Editing Queue
                composable<Route.EditingQueue> {
                    PlaceholderScreen(
                        title = "🎨 Editing Queue",
                        subtitle = "Welcome, ${SessionManager.currentUser?.name}!",
                        role = SessionManager.currentUser?.role?.name ?: "",
                        onLogout = { handleLogout(navController) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    role: String,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title,    fontSize = 32.sp)
            Text(text = subtitle, fontSize = 18.sp)
            Text(
                text  = "Role: $role",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}

/**
 * Helper function to handle logout logic consistently
 */
private fun handleLogout(navController: NavController) {
    SessionManager.clear()
    navController.navigate(Route.Login) {
        // Clears the entire backstack so user can't go back to dashboard
        popUpTo(0) { inclusive = true }
    }
}