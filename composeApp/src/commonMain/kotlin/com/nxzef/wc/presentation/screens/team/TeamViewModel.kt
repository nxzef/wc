package com.nxzef.wc.presentation.screens.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.domain.usecase.team.CreateTeamMemberUseCase
import com.nxzef.wc.domain.usecase.team.DeleteTeamMemberUseCase
import com.nxzef.wc.domain.usecase.team.GetTeamUseCase
import com.nxzef.wc.shared.util.ErrorMessages
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeamUseCase: GetTeamUseCase,
    private val createTeamMemberUseCase: CreateTeamMemberUseCase,
    private val deleteTeamMemberUseCase: DeleteTeamMemberUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TeamState())
    val state: StateFlow<TeamState> = _state.asStateFlow()

    private val _uiEvent = Channel<TeamUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTeam()
    }

    fun onAction(action: TeamAction) {
        when (action) {
            TeamAction.LoadTeam -> loadTeam()
            TeamAction.ShowAddDialog ->
                _state.update { it.copy(showAddDialog = true) }

            TeamAction.HideAddDialog ->
                _state.update { it.copy(showAddDialog = false) }

            is TeamAction.OnNameChange ->
                _state.update { it.copy(newName = action.value) }

            is TeamAction.OnEmailChange ->
                _state.update { it.copy(newEmail = action.value) }

            is TeamAction.OnRoleChange ->
                _state.update { it.copy(newRole = action.value) }

            TeamAction.OnCreateMember -> createMember()

            is TeamAction.ShowDeleteDialog ->
                _state.update { it.copy(showDeleteDialog = action.user) }

            TeamAction.HideDeleteDialog ->
                _state.update { it.copy(showDeleteDialog = null) }

            is TeamAction.OnDeleteMember -> deleteMember(action.id)

            TeamAction.DismissInviteDialog ->
                _state.update { it.copy(showInviteDialog = null) }
        }
    }

    private fun deleteMember(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            deleteTeamMemberUseCase(id)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = null
                        )
                    }
                    _uiEvent.send(TeamUiEvent.ShowSnackbar("Member removed"))
                    loadTeam()
                }
                .onFailure { e ->
                    _state.update { it.copy(isDeleting = false) }
                    _uiEvent.send(
                        TeamUiEvent.ShowSnackbar(
                            ErrorMessages.forGeneric(e.message)
                        )
                    )
                }
        }
    }

    private fun loadTeam() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getTeamUseCase()
                .onSuccess { team ->
                    _state.update {
                        it.copy(team = team, isLoading = false)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.send(
                        TeamUiEvent.ShowSnackbar(
                            ErrorMessages.forGeneric(e.message)
                        )
                    )
                }
        }
    }

    private fun createMember() {
        val s = _state.value
        if (s.newName.isBlank() || s.newEmail.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(
                    TeamUiEvent.ShowSnackbar("All fields are required")
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }
            createTeamMemberUseCase(
                name = s.newName.trim(),
                email = s.newEmail.trim(),
                role = s.newRole.name
            ).onSuccess { user ->
                val team = SessionManager.getTeam()
                val invited = team?.let {
                    InvitedMember(
                        name = user.name,
                        email = user.email,
                        inviteCode = it.inviteCode
                    )
                }
                _state.update {
                    it.copy(
                        isCreating = false,
                        showAddDialog = false,
                        newName = "",
                        newEmail = "",
                        newRole = com.nxzef.wc.shared.model.UserRole.LEAD_MANAGER,
                        showInviteDialog = invited
                    )
                }
                _uiEvent.send(TeamUiEvent.MemberCreated)
                loadTeam()
            }.onFailure { e ->
                _state.update { it.copy(isCreating = false) }
                _uiEvent.send(
                    TeamUiEvent.ShowSnackbar(
                        ErrorMessages.forGeneric(e.message)
                    )
                )
            }
        }
    }
}