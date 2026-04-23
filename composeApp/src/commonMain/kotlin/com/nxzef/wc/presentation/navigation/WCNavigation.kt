package com.nxzef.wc.presentation.navigation

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
//    val user by sessionManager.currentUser.collectAsState()

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
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 8 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    ) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 8 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    ) +
                    scaleOut(
                        targetScale = 1.04f,
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 8 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    ) +
                    scaleIn(
                        initialScale = 1.04f,
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideOutHorizontally(
                        targetOffsetX = { it / 8 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    ) +
                    scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        }
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