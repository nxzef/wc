package com.nxzef.wc.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.components.WCPermanentSidebar
import com.nxzef.wc.presentation.screens.auth.LoginScreen
import com.nxzef.wc.presentation.screens.bookings.BookingScreen
import com.nxzef.wc.presentation.screens.dashboard.DashboardScreen
import com.nxzef.wc.presentation.screens.editor.EditorScreen
import com.nxzef.wc.presentation.screens.invoices.InvoiceScreen
import com.nxzef.wc.presentation.screens.leads.AddLeadScreen
import com.nxzef.wc.presentation.screens.leads.LeadPipelineScreen
import com.nxzef.wc.presentation.screens.marketing.MarketingScreen
import com.nxzef.wc.presentation.screens.photographer.PhotographerScreen
import com.nxzef.wc.presentation.screens.quotes.QuoteScreen
import com.nxzef.wc.presentation.screens.settings.SettingsScreen
import com.nxzef.wc.presentation.screens.team.TeamScreen
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.koinInject

@Composable
fun WCNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = getCurrentRoute(navBackStackEntry)
    val sessionManager: SessionManager = koinInject()
    val user by sessionManager.currentUser.collectAsState()

    var isSidebarCollapsed by remember { mutableStateOf(false) }

    WCTheme {
        Surface {
            if (currentRoute == Route.Login) {
                AppNavHost(navController = navController)
            } else {
                WCPermanentSidebar(
                    currentRoute = currentRoute,
                    isCollapsed = isSidebarCollapsed,
                    onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo<Route.OwnerDashboard> { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        sessionManager.clear()
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
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
            LeadPipelineScreen(
                onBack = { navController.popBackStack() },
                onAddLead = {
                    navController.navigate(Route.AddLead)
                },
                onViewQuotes = { leadId ->
                    navController.navigate(Route.Quotes(leadId))
                }
            )
        }

        composable<Route.AddLead> {
            AddLeadScreen(
                onLeadCreated = {
                    navController.navigate(Route.LeadPipeline) {
                        popUpTo<Route.AddLead> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Marketing> {
            MarketingScreen(
                onBack = { navController.popBackStack() },
                onAddLead = {
                    navController.navigate(Route.AddLead)
                }
            )
        }

        composable<Route.MyShoots> {
            PhotographerScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.EditingQueue> {
            EditorScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.TeamManagement> {
            TeamScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Invoices> {
            InvoiceScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Bookings> {
            BookingScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Quotes> { backStackEntry ->
            val route: Route.Quotes = backStackEntry.toRoute()
            QuoteScreen(
                leadId = route.leadId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun getRouteTitle(route: Route): String {
    return when (route) {
        Route.OwnerDashboard -> "Dashboard"
        Route.LeadPipeline -> "Lead Pipeline"
        Route.AddLead -> "Add Lead"
        Route.Marketing -> "Marketing"
        Route.MyShoots -> "My Shoots"
        Route.EditingQueue -> "Editing Queue"
        Route.TeamManagement -> "Team Management"
        Route.Invoices -> "Invoices"
        Route.Bookings -> "Bookings"
        Route.Settings -> "Settings"
        is Route.Quotes -> "Quotes"
        Route.Login -> ""
    }
}

fun getCurrentRoute(backStackEntry: NavBackStackEntry?): Route {
    val destination = backStackEntry?.destination ?: return Route.Login
    return when {
        destination.hasRoute<Route.Login>() -> Route.Login
        destination.hasRoute<Route.OwnerDashboard>() -> Route.OwnerDashboard
        destination.hasRoute<Route.LeadPipeline>() -> Route.LeadPipeline
        destination.hasRoute<Route.AddLead>() -> Route.AddLead
        destination.hasRoute<Route.Marketing>() -> Route.Marketing
        destination.hasRoute<Route.MyShoots>() -> Route.MyShoots
        destination.hasRoute<Route.EditingQueue>() -> Route.EditingQueue
        destination.hasRoute<Route.TeamManagement>() -> Route.TeamManagement
        destination.hasRoute<Route.Invoices>() -> Route.Invoices
        destination.hasRoute<Route.Bookings>() -> Route.Bookings
        destination.hasRoute<Route.Settings>() -> Route.Settings
        destination.hasRoute<Route.Quotes>() -> {
            val leadId = backStackEntry.toRoute<Route.Quotes>().leadId
            Route.Quotes(leadId)
        }
        else -> Route.Login
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title Coming soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
