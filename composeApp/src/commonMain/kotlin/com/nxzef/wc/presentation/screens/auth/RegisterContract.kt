package com.nxzef.wc.presentation.screens.auth

import com.nxzef.wc.shared.model.UserRole

data class RegisterState(
    val name: String = "",
    val teamName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false
)

sealed interface RegisterAction {
    data class OnNameChange(val name: String) : RegisterAction
    data class OnTeamNameChange(val teamName: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterAction
    data object OnSubmit : RegisterAction
}

sealed interface RegisterUiEvent {
    data class ShowSnackbar(val message: String) : RegisterUiEvent
    data class NavigateToHome(val role: UserRole) : RegisterUiEvent
}
