package com.nxzef.wc.data.session

import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun save(token: String, user: User) {
        _token.value      = token
        _currentUser.value = user
        _isLoggedIn.value  = true
    }

    fun clear() {
        _token.value       = null
        _currentUser.value = null
        _isLoggedIn.value  = false
    }

    fun getToken()       : String?   = _token.value
    fun getUser()        : User?     = _currentUser.value
    fun getRole()        : UserRole? = _currentUser.value?.role
    fun isLoggedInSync() : Boolean   = _token.value != null
}