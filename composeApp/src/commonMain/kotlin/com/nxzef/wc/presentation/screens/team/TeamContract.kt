package com.nxzef.wc.presentation.screens.team

import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.model.UserRole

data class TeamState(
    val team: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newEmail: String = "",
    val newRole: UserRole = UserRole.LEAD_MANAGER,
    val isCreating: Boolean = false,
    val showDeleteDialog: User? = null,
    val isDeleting: Boolean = false,
    val showInviteDialog: InvitedMember? = null
)

data class InvitedMember(
    val name: String,
    val email: String,
    val inviteCode: String
)

sealed interface TeamAction {
    data object LoadTeam : TeamAction
    data object ShowAddDialog : TeamAction
    data object HideAddDialog : TeamAction
    data class ShowDeleteDialog(val user: User) : TeamAction
    data object HideDeleteDialog : TeamAction
    data class OnNameChange(val value: String) : TeamAction
    data class OnEmailChange(val value: String) : TeamAction
    data class OnRoleChange(val value: UserRole) : TeamAction
    data object OnCreateMember : TeamAction
    data class OnDeleteMember(val id: String) : TeamAction
    data object DismissInviteDialog : TeamAction
}

sealed interface TeamUiEvent {
    data class ShowSnackbar(val message: String) : TeamUiEvent
    data object MemberCreated : TeamUiEvent
}