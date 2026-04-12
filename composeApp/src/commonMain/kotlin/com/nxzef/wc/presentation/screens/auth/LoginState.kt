package com.nxzef.wc.presentation.screens.auth

import com.nxzef.wc.data.model.UserRole

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole) : LoginState()
    data class Error(val message: String) : LoginState()
}