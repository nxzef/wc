package com.nxzef.wc.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nxzef.wc.data.model.UserRole
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.WCPermanentSidebar
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.screens.auth.LoginScreen
import com.nxzef.wc.presentation.screens.dashboard.DashboardScreen
import com.nxzef.wc.presentation.screens.leads.LeadPipelineScreen
import com.nxzef.wc.presentation.theme.WCTheme

@Composable
fun WCNavigation() {
    val navController = rememberNavController()

    WCTheme {
        Surface {
            NavHost(
                navController = navController,
                startDestination = Route.Login
            ) {
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
                                popUpTo<Route.Login> { inclusive = true }
                            }
                        }
                    )
                }

                composable<Route.OwnerDashboard> {
                    DashboardScreen(
                        currentRoute = Route.OwnerDashboard,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.LeadPipeline> {
                    LeadPipelineScreen(
                        currentRoute = Route.LeadPipeline,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.Marketing> {
                    PlaceholderScreen(
                        title = "📣 Marketing",
                        currentRoute = Route.Marketing,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.MyShoots> {
                    PlaceholderScreen(
                        title = "📸 My Shoots",
                        currentRoute = Route.MyShoots,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.EditingQueue> {
                    PlaceholderScreen(
                        title = "🎨 Editing Queue",
                        currentRoute = Route.EditingQueue,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.TeamManagement> {
                    PlaceholderScreen(
                        title = "👥 Team Management",
                        currentRoute = Route.TeamManagement,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.Invoices> {
                    PlaceholderScreen(
                        title = "🧾 Invoices",
                        currentRoute = Route.Invoices,
                        onNavigate = { navController.navigate(it) },
                        onLogout = { handleLogout(navController) }
                    )
                }

                composable<Route.Settings> {
                    PlaceholderScreen(
                        title = "⚙️ Settings",
                        currentRoute = Route.Settings,
                        onNavigate = { navController.navigate(it) },
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
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit
) {
    WCPermanentSidebar(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WCTopBar(title = title)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun handleLogout(
    navController: androidx.navigation.NavController
) {
    SessionManager.clear()
    navController.navigate(Route.Login) {
        popUpTo(0) { inclusive = true }
    }
}