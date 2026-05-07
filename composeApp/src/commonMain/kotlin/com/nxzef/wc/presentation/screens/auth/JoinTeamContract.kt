package com.nxzef.wc.presentation.screens.auth

import com.nxzef.wc.shared.model.UserRole

data class JoinTeamState(
    val email: String = "",
    val inviteCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false
)

sealed interface JoinTeamAction {
    data class OnEmailChange(val email: String) : JoinTeamAction
    data class OnInviteCodeChange(val inviteCode: String) : JoinTeamAction
    data class OnNewPasswordChange(val newPassword: String) : JoinTeamAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : JoinTeamAction
    data object OnSubmit : JoinTeamAction
}

sealed interface JoinTeamUiEvent {
    data class ShowSnackbar(val message: String) : JoinTeamUiEvent
    data class NavigateToHome(val role: UserRole) : JoinTeamUiEvent
}
