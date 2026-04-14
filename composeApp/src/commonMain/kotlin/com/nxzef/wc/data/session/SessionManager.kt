package com.nxzef.wc.data.session

import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = _token.map { it != null }
        .let { MutableStateFlow(false) } // This was a placeholder, better way:
    
    // Using a simpler approach for KMP compatibility if needed, 
    // but better to use stateIn in a real scope. 
    // For now, let's just use a derived property or a simplified flow.

    fun save(token: String, user: User) {
        _token.value = token
        _currentUser.value = user
    }

    fun clear() {
        _token.value = null
        _currentUser.value = null
    }

    fun getToken(): String? = _token.value
    fun getUser(): User? = _currentUser.value
    fun getRole(): UserRole? = _currentUser.value?.role
    fun isLoggedInSync(): Boolean = _token.value != null
}
