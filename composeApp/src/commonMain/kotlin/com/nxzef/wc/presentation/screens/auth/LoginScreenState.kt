package com.nxzef.wc.presentation.screens.auth

data class LoginScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)