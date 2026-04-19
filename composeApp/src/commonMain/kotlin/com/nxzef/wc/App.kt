package com.nxzef.wc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.navigation.WCNavigation
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun App() {
    val tokenStorage: TokenStorage = koinInject()
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        restoreSession(tokenStorage)
        isReady = true
    }

    if (isReady) {
        WCNavigation()
    }
}

private suspend fun restoreSession(tokenStorage: TokenStorage) {
    try {
        val prefs = tokenStorage.dataStore.data.first()
        val token = prefs[TokenStorage.TOKEN_KEY]
        val id = prefs[TokenStorage.USER_ID_KEY]
        val name = prefs[TokenStorage.USER_NAME_KEY]
        val email = prefs[TokenStorage.USER_EMAIL_KEY]
        val role = prefs[TokenStorage.USER_ROLE_KEY]

        if (token != null && id != null &&
            name != null && email != null && role != null
        ) {
            val user = User(
                id = id,
                name = name,
                email = email,
                role = UserRole.valueOf(role),
                isActive = true
            )
            SessionManager.save(token, user)
        }
    } catch (_: Exception) {
        // No stored session — fresh start
    }
}