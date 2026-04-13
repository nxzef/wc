package com.nxzef.wc.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nxzef.wc.data.model.UserRole
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.navigation.Route

data class SidebarNavItem(
    val icon  : ImageVector,
    val label : String,
    val route : Route,
    val roles : List<UserRole> = UserRole.entries
)

val sidebarItems = listOf(
    SidebarNavItem(
        icon  = Icons.Default.Dashboard,
        label = "Dashboard",
        route = Route.OwnerDashboard,
        roles = listOf(UserRole.OWNER)
    ),
    SidebarNavItem(
        icon  = Icons.Default.People,
        label = "Leads",
        route = Route.LeadPipeline,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon  = Icons.AutoMirrored.Filled.TrendingUp,
        label = "Marketing",
        route = Route.Marketing,
        roles = listOf(UserRole.OWNER, UserRole.MARKETING)
    ),
    SidebarNavItem(
        icon  = Icons.Default.Receipt,
        label = "Invoices",
        route = Route.Invoices,
        roles = listOf(UserRole.OWNER, UserRole.LEAD_MANAGER)
    ),
    SidebarNavItem(
        icon  = Icons.Default.CameraAlt,
        label = "My Shoots",
        route = Route.MyShoots,
        roles = listOf(UserRole.OWNER, UserRole.PHOTOGRAPHER)
    ),
    SidebarNavItem(
        icon  = Icons.Default.Edit,
        label = "Editing Queue",
        route = Route.EditingQueue,
        roles = listOf(UserRole.OWNER, UserRole.EDITOR)
    ),
    SidebarNavItem(
        icon  = Icons.Default.Group,
        label = "Team",
        route = Route.TeamManagement,
        roles = listOf(UserRole.OWNER)
    ),
    SidebarNavItem(
        icon  = Icons.Default.Settings,
        label = "Settings",
        route = Route.Settings,
        roles = listOf(UserRole.OWNER)
    )
)

@Composable
fun WCPermanentSidebar(
    currentRoute : Route,
    onNavigate   : (Route) -> Unit,
    onLogout     : () -> Unit,
    content      : @Composable () -> Unit
) {
    val userRole     = SessionManager.currentUser?.role
    val visibleItems = sidebarItems.filter { item ->
        userRole != null && userRole in item.roles
    }

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier      = Modifier.width(240.dp),
                drawerShape   = RoundedCornerShape(0.dp),
                drawerTonalElevation = 2.dp
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = "☁️",
                        fontSize   = 24.sp
                    )
                    Column {
                        Text(
                            text       = "Wedding Clouds",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text  = "Management Suite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                // User card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier           = Modifier.size(32.dp),
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text  = SessionManager.currentUser?.name ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text  = userRole?.name ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme
                                    .onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nav items
                visibleItems.forEach { item ->
                    val selected = currentRoute::class == item.route::class
                    NavigationDrawerItem(
                        icon    = {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label   = {
                            Text(
                                text  = item.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = selected,
                        onClick  = { onNavigate(item.route) },
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical   = 2.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Logout
                NavigationDrawerItem(
                    icon  = {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint               = MaterialTheme.colorScheme.error
                        )
                    },
                    label = {
                        Text(
                            text  = "Logout",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = false,
                    onClick  = onLogout,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical   = 8.dp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        content = content
    )
}