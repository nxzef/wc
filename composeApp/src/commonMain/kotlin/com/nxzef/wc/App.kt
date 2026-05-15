package com.nxzef.wc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nxzef.wc.config.AppConfig
import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.remote.AppVersionService
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.platform.openUrl
import com.nxzef.wc.presentation.navigation.WCNavigation
import com.nxzef.wc.presentation.theme.ThemeManager
import com.nxzef.wc.shared.model.Team
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun App() {
    val tokenStorage: TokenStorage = koinInject()
    val appVersionService: AppVersionService = koinInject()
    var isReady by remember { mutableStateOf(false) }
    var isFreshInstall by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        restoreSession(tokenStorage)
        ThemeManager.setTheme(tokenStorage.getAppTheme())
        val launched = tokenStorage.hasLaunchedBefore()
        isFreshInstall = !launched
        if (!launched) {
            tokenStorage.markLaunchedBefore()
        }
        try {
            val versionInfo = appVersionService.checkVersion()
            val latest = versionInfo?.get("latest_version")
            val url = versionInfo?.get("download_url") ?: ""
            if (latest != null && latest != AppConfig.CURRENT_VERSION) {
                updateUrl = url
                showUpdateDialog = true
            }
        } catch (_: Exception) { }
        isReady = true
    }

    if (isReady) {
        WCNavigation(isFreshInstall = isFreshInstall)
    } else {
        WCSplashScreen()
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Available") },
            text = { Text("A new version of WeddingClouds is available. Download and install to get the latest features and fixes.") },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    openUrl(updateUrl)
                }) { Text("Download Update") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
private fun WCSplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1513)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "The Wedding Clouds",
            color = Color(0xFF83D5C5),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Wedding Photography CRM",
            color = Color(0xFF83D5C5).copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp
        )
    }
}

private suspend fun restoreSession(tokenStorage: TokenStorage) {
    try {
        val prefs = tokenStorage.dataStore.data.first()
        val token        = prefs[TokenStorage.TOKEN_KEY]
        val refreshToken = prefs[TokenStorage.REFRESH_TOKEN_KEY]
        val id           = prefs[TokenStorage.USER_ID_KEY]
        val name         = prefs[TokenStorage.USER_NAME_KEY]
        val email        = prefs[TokenStorage.USER_EMAIL_KEY]
        val role         = prefs[TokenStorage.USER_ROLE_KEY]
        val teamId       = prefs[TokenStorage.TEAM_ID_KEY]
        val teamName     = prefs[TokenStorage.TEAM_NAME_KEY]
        val teamInviteCode = prefs[TokenStorage.TEAM_INVITE_CODE_KEY]

        if (token != null && refreshToken != null && id != null &&
            name != null && email != null && role != null
        ) {
            val user = User(
                id = id,
                name = name,
                email = email,
                role = UserRole.valueOf(role),
                isActive = true,
                teamId = teamId
            )
            val team = if (teamId != null && teamName != null && teamInviteCode != null) {
                Team(
                    id = teamId,
                    name = teamName,
                    ownerId = "",
                    inviteCode = teamInviteCode,
                    isActive = true
                )
            } else null
            SessionManager.save(token, refreshToken, user, team)
        }
    } catch (_: Exception) {
        // No stored session — fresh start
    }
}
