package com.nxzef.wc.presentation.navigation

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import com.nxzef.wc.presentation.components.WCPermanentSidebar
import com.nxzef.wc.presentation.screens.auth.ForgotPasswordScreen
import com.nxzef.wc.presentation.screens.auth.JoinTeamScreen
import com.nxzef.wc.presentation.screens.auth.LoginScreen
import com.nxzef.wc.presentation.screens.auth.RegisterScreen
import com.nxzef.wc.presentation.screens.auth.WelcomeScreen
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
import com.nxzef.wc.presentation.screens.expenses.ProjectExpensesScreen
import com.nxzef.wc.presentation.screens.tasks.TasksScreen
import com.nxzef.wc.presentation.screens.team.TeamScreen
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.koinInject

private fun roleHome(role: UserRole): Route = when (role) {
    UserRole.OWNER -> Route.OwnerDashboard
    UserRole.LEAD_MANAGER -> Route.LeadPipeline
    UserRole.MARKETING -> Route.Marketing
    UserRole.PHOTOGRAPHER -> Route.MyShoots
    UserRole.EDITOR -> Route.EditingQueue
}

private fun isAuthRoute(route: Route): Boolean = when (route) {
    Route.Welcome, Route.Register, Route.JoinTeam, Route.Login, Route.ForgotPassword -> true
    else -> false
}

@Composable
fun WCNavigation(isFreshInstall: Boolean = false) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = getCurrentRoute(navBackStackEntry)
    val tokenStorage: TokenStorage = koinInject()
    val authRepository: AuthRepository = koinInject()
    val scope = rememberCoroutineScope()

    val initialRoute = remember {
        val user = SessionManager.getUser()
        when {
            user != null -> roleHome(user.role)
            isFreshInstall -> Route.Welcome
            else -> Route.Login
        }
    }

    var isSidebarCollapsed by remember { mutableStateOf(false) }

    val isLoggedIn by SessionManager.isLoggedIn.collectAsStateWithLifecycle()
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            val dest = navController.currentBackStackEntry?.destination
            if (dest != null && !dest.hasRoute<Route.Welcome>() &&
                !dest.hasRoute<Route.Login>() &&
                !dest.hasRoute<Route.Register>() &&
                !dest.hasRoute<Route.JoinTeam>() &&
                !dest.hasRoute<Route.ForgotPassword>()
            ) {
                tokenStorage.clearSession()
                navController.navigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val navigateToTab: (Route) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    WCTheme {
        Surface {
            if (isAuthRoute(currentRoute)) {
                AppNavHost(
                    navController = navController,
                    startDestination = initialRoute,
                    onNavigateToTab = navigateToTab
                )
            } else {
                WCPermanentSidebar(
                    currentRoute = currentRoute,
                    isCollapsed = isSidebarCollapsed,
                    onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    onNavigate = navigateToTab,
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                        }
                    }
                ) {
                    AppNavHost(
                        navController = navController,
                        startDestination = initialRoute,
                        onNavigateToTab = navigateToTab,
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
    startDestination: Route = Route.Welcome,
    onNavigateToTab: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    val onAuthSuccess: (UserRole) -> Unit = { role ->
        navController.navigate(roleHome(role)) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = tween(300, easing = EaseOutQuart)
                    )
        }
    ) {
        composable<Route.Welcome> {
            WelcomeScreen(
                onCreateCompany = { navController.navigate(Route.Register) },
                onJoinTeam = { navController.navigate(Route.JoinTeam) },
                onSignIn = { navController.navigate(Route.Login) }
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                onRegistered = onAuthSuccess,
                onBack = { navController.popBackStack() },
                onSignIn = { navController.navigate(Route.Login) }
            )
        }

        composable<Route.JoinTeam> {
            JoinTeamScreen(
                onJoined = onAuthSuccess,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Login> {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onCreateCompany = { navController.navigate(Route.Register) },
                onJoinTeam = { navController.navigate(Route.JoinTeam) },
                onForgotPassword = { navController.navigate(Route.ForgotPassword) }
            )
        }

        composable<Route.ForgotPassword> {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.OwnerDashboard> {
            DashboardScreen(
                onNavigateToPipeline = { onNavigateToTab(Route.LeadPipeline) },
                onNavigateToInvoices = { onNavigateToTab(Route.Invoices) },
                onNavigateToTasks = { onNavigateToTab(Route.Tasks) },
                onNavigateToBookings = { onNavigateToTab(Route.Bookings) },
                onViewLeads = { onNavigateToTab(Route.LeadPipeline) },
                onNavigateToAddLead = { navController.navigate(Route.AddLead) }
            )
        }

        composable<Route.LeadPipeline> {
            LeadPipelineScreen(
                onBack = { navController.popBackStack() },
                onAddLead = { navController.navigate(Route.AddLead) },
                onViewQuotes = { leadId, clientName, clientEmail ->
                    navController.navigate(Route.Quotes(leadId, clientName, clientEmail))
                },
                onViewBooking = { onNavigateToTab(Route.Bookings) }
            )
        }

        composable<Route.AddLead> {
            AddLeadScreen(
                onLeadCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Marketing> {
            MarketingScreen(
                onBack = { navController.popBackStack() },
                onAddLead = { navController.navigate(Route.AddLead) }
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
            BookingScreen(
                onBack = { navController.popBackStack() },
                onExpenses = { bookingId ->
                    navController.navigate(Route.ProjectExpenses(bookingId))
                }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Quotes> { backStackEntry ->
            val route: Route.Quotes = backStackEntry.toRoute()
            QuoteScreen(
                leadId = route.leadId,
                clientName = route.clientName,
                clientEmail = route.clientEmail,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Tasks> {
            TasksScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.ProjectExpenses> { backStackEntry ->
            val route: Route.ProjectExpenses = backStackEntry.toRoute()
            ProjectExpensesScreen(
                bookingId = route.bookingId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun getCurrentRoute(backStackEntry: NavBackStackEntry?): Route {
    val destination = backStackEntry?.destination ?: return Route.Welcome
    return when {
        destination.hasRoute<Route.Welcome>() -> Route.Welcome
        destination.hasRoute<Route.Register>() -> Route.Register
        destination.hasRoute<Route.JoinTeam>() -> Route.JoinTeam
        destination.hasRoute<Route.Login>() -> Route.Login
        destination.hasRoute<Route.ForgotPassword>() -> Route.ForgotPassword
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
        destination.hasRoute<Route.Tasks>() -> Route.Tasks
        destination.hasRoute<Route.ProjectExpenses>() -> {
            val bookingId = backStackEntry.toRoute<Route.ProjectExpenses>().bookingId
            Route.ProjectExpenses(bookingId)
        }
        destination.hasRoute<Route.Quotes>() -> {
            val q = backStackEntry.toRoute<Route.Quotes>()
            Route.Quotes(q.leadId, q.clientName, q.clientEmail)
        }

        else -> Route.Welcome
    }
}
