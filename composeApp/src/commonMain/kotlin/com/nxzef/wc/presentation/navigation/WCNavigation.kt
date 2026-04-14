package com.nxzef.wc.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.AuthRepository
import com.nxzef.wc.presentation.components.WCPermanentSidebar
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.screens.auth.LoginScreen
import com.nxzef.wc.presentation.screens.dashboard.DashboardScreen
import com.nxzef.wc.presentation.screens.leads.LeadPipelineScreen
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun WCNavigation(
    authRepository: AuthRepository = koinInject()
) {
    val navController = rememberNavController()
    val isLoggedIn by authRepository.isLoggedIn.collectAsState()

    WCTheme {
        Surface {
            if (isLoggedIn) {
                val backStackEntry by navController
                    .currentBackStackEntryAsState()
                val currentRoute = getCurrentRoute(backStackEntry)

                WCPermanentSidebar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = { 
                        handleLogout(navController, authRepository) 
                    }
                ) {
                    AppNavHost(navController = navController)
                }
            } else {
                AppNavHost(navController = navController)
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
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
            DashboardScreen()
        }

        composable<Route.LeadPipeline> {
            LeadPipelineScreen()
        }

        composable<Route.Marketing> {
            PlaceholderScreen(title = "📣 Marketing")
        }

        composable<Route.MyShoots> {
            PlaceholderScreen(title = "📸 My Shoots")
        }

        composable<Route.EditingQueue> {
            PlaceholderScreen(title = "🎨 Editing Queue")
        }

        composable<Route.TeamManagement> {
            PlaceholderScreen(title = "👥 Team Management")
        }

        composable<Route.Invoices> {
            PlaceholderScreen(title = "🧾 Invoices")
        }

        composable<Route.Settings> {
            PlaceholderScreen(title = "⚙️ Settings")
        }
    }
}

// Helper to get current route object from backstack
@Composable
private fun getCurrentRoute(
    backStackEntry: androidx.navigation.NavBackStackEntry?
): Route {
    val destination = backStackEntry?.destination ?: return Route.OwnerDashboard
    
    return when {
        destination.hasRoute<Route.OwnerDashboard>() -> Route.OwnerDashboard
        destination.hasRoute<Route.LeadPipeline>() -> Route.LeadPipeline
        destination.hasRoute<Route.Marketing>() -> Route.Marketing
        destination.hasRoute<Route.MyShoots>() -> Route.MyShoots
        destination.hasRoute<Route.EditingQueue>() -> Route.EditingQueue
        destination.hasRoute<Route.TeamManagement>() -> Route.TeamManagement
        destination.hasRoute<Route.Invoices>() -> Route.Invoices
        destination.hasRoute<Route.Settings>() -> Route.Settings
        else -> Route.OwnerDashboard
    }
}

@Composable
fun PlaceholderScreen(title: String) {
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

private fun handleLogout(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    MainScope().launch {
        authRepository.logout()
        navController.navigate(Route.Login) {
            popUpTo(0) { inclusive = true }
        }
    }
}