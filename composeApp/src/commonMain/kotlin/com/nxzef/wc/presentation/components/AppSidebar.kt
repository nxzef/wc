package com.nxzef.wc.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.navigation.Route
import com.nxzef.wc.shared.model.UserRole
import org.koin.compose.koinInject

data class SidebarNavItem(
    val icon: ImageVector,
    val label: String,
    val route: Route,
    val roles: List<UserRole> = UserRole.entries
)

val sidebarItems = listOf(
    SidebarNavItem(
        icon = Icons.Default.Dashboard,
        label = "Dashboard",
        route = Route.OwnerDashboard,
        roles = listOf(UserRole.OWNER)
    ),
    SidebarNavItem(
        icon = Icons.Default.People,
        label = "Leads",
        route = Route.LeadPipeline,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon = Icons.Default.CalendarMonth,
        label = "Bookings",
        route = Route.Bookings,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon  = Icons.Default.CheckBox,
        label = "Tasks",
        route = Route.Tasks,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        label = "Marketing",
        route = Route.Marketing,
        roles = listOf(UserRole.OWNER, UserRole.MARKETING)
    ),
    SidebarNavItem(
        icon = Icons.Default.Receipt,
        label = "Invoices",
        route = Route.Invoices,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon = Icons.Default.CameraAlt,
        label = "My Shoots",
        route = Route.MyShoots,
        roles = listOf(UserRole.OWNER, UserRole.PHOTOGRAPHER)
    ),
    SidebarNavItem(
        icon = Icons.Default.Edit,
        label = "Editing Queue",
        route = Route.EditingQueue,
        roles = listOf(UserRole.OWNER, UserRole.EDITOR)
    ),
    SidebarNavItem(
        icon = Icons.Default.Analytics,
        label = "Analytics",
        route = Route.Analytics,
        roles = listOf(UserRole.OWNER)
    ),
    SidebarNavItem(
        icon = Icons.Default.Group,
        label = "Team",
        route = Route.TeamManagement,
        roles = listOf(UserRole.OWNER)
    ),
    SidebarNavItem(
        icon = Icons.Default.Settings,
        label = "Settings",
        route = Route.Settings,
        roles = UserRole.entries
    )
)

@Composable
fun WCPermanentSidebar(
    currentRoute: Route,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit,
    sessionManager: SessionManager = koinInject(),
    content: @Composable () -> Unit
) {
    val user by sessionManager.currentUser.collectAsState()
    val userRole = user?.role

    val visibleItems = sidebarItems.filter { item ->
        userRole != null && userRole in item.roles
    }

    val drawerWidth by animateDpAsState(targetValue = if (isCollapsed) 80.dp else 240.dp)

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(drawerWidth).zIndex(0f).fillMaxHeight(),
                drawerShape = RoundedCornerShape(0.dp),
                drawerTonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle & Brand Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.spacedBy(
                            8.dp
                        )
                    ) {
                        if (!isCollapsed) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WC ARCHIVE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "EDITORIAL SYSTEMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        IconButton(onClick = onToggleCollapse) {
                            Icon(
                                imageVector = if (isCollapsed) Icons.Default.Menu else Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = "Toggle Sidebar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // User card
                    user?.let { currentUser ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.spacedBy(
                                    10.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (!isCollapsed) {
                                    Column {
                                        Text(
                                            text = currentUser.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = currentUser.role.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme
                                                .onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nav items
                    visibleItems.forEach { item ->
                        val selected = currentRoute::class == item.route::class
                        
                        NavigationDrawerItem(
                            icon = {
                                Box(
                                    modifier = if (isCollapsed) Modifier.fillMaxWidth() else Modifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = if (isCollapsed) Modifier.size(24.dp) else Modifier
                                    )
                                }
                            },
                            label = {
                                if (!isCollapsed) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            modifier = if (isCollapsed) {
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            } else {
                                Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isCollapsed) 0.3f else 1f),
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Logout
                    NavigationDrawerItem(
                        icon = {
                            Box(
                                modifier = if (isCollapsed) Modifier.fillMaxWidth() else Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = if (isCollapsed) Modifier.size(24.dp) else Modifier
                                )
                            }
                        },
                        label = {
                            if (!isCollapsed) {
                                Text(
                                    text = "Logout",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        selected = false,
                        onClick = onLogout,
                        modifier = if (isCollapsed) {
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        } else {
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        content = content
    )
}
