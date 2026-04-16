package com.nxzef.wc.presentation.screens.auth

import com.nxzef.wc.shared.model.UserRole

/**
 * State of the Login screen
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * User actions/intents triggered from the UI
 */
sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    data object OnLoginClick : LoginAction
}

/**
 * One-time side effects (Navigation, Snackbars, etc.)
 */
sealed interface LoginUiEvent {
    data class ShowSnackbar(val message: String) : LoginUiEvent
    data class NavigateToHome(val role: UserRole) : LoginUiEvent
}
