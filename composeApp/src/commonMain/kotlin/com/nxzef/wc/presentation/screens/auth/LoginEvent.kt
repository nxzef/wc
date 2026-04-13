package com.nxzef.wc.presentation.screens.auth

import com.nxzef.wc.data.model.UserRole

/**
 * User actions triggered from the UI
 */
sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object Login : LoginAction
}

/**
 * Transient side effects handled by the UI (Navigation, Snackbars)
 */
sealed interface LoginUiEvent {
    data class ShowSnackbar(val message: String) : LoginUiEvent
    data class NavigateToHome(val role: UserRole) : LoginUiEvent
}