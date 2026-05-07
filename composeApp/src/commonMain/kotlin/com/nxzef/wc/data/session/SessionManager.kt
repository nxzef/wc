package com.nxzef.wc.data.session

import com.nxzef.wc.shared.model.Team
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentTeam = MutableStateFlow<Team?>(null)
    val currentTeam: StateFlow<Team?> = _currentTeam.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun save(token: String, refreshToken: String, user: User, team: Team? = null) {
        _token.value        = token
        _refreshToken.value = refreshToken
        _currentUser.value  = user
        _currentTeam.value  = team
        _isLoggedIn.value   = true
    }

    fun clear() {
        _token.value        = null
        _refreshToken.value = null
        _currentUser.value  = null
        _currentTeam.value  = null
        _isLoggedIn.value   = false
    }

    fun getToken()        : String?   = _token.value
    fun getRefreshToken() : String?   = _refreshToken.value
    fun getUser()         : User?     = _currentUser.value
    fun getTeam()         : Team?     = _currentTeam.value
    fun getTeamId()       : String?   = _currentTeam.value?.id ?: _currentUser.value?.teamId
    fun getRole()         : UserRole? = _currentUser.value?.role
    fun isLoggedInSync()  : Boolean   = _token.value != null
}
