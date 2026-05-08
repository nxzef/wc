package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.auth.JoinTeamUseCase
import com.nxzef.wc.shared.util.AppResult
import com.nxzef.wc.shared.util.ErrorMessages
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinTeamViewModel(
    private val joinTeamUseCase: JoinTeamUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JoinTeamState())
    val state: StateFlow<JoinTeamState> = _state.asStateFlow()

    private val _uiEvent = Channel<JoinTeamUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: JoinTeamAction) {
        when (action) {
            is JoinTeamAction.OnEmailChange ->
                _state.update { it.copy(email = action.email) }
            is JoinTeamAction.OnInviteCodeChange ->
                _state.update { it.copy(inviteCode = action.inviteCode.uppercase().take(6)) }
            is JoinTeamAction.OnNewPasswordChange ->
                _state.update { it.copy(newPassword = action.newPassword) }
            is JoinTeamAction.OnConfirmPasswordChange ->
                _state.update { it.copy(confirmPassword = action.confirmPassword) }
            JoinTeamAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = joinTeamUseCase(
                email = s.email,
                inviteCode = s.inviteCode,
                newPassword = s.newPassword,
                confirmPassword = s.confirmPassword
            )) {
                is AppResult.Success -> {
                    _uiEvent.send(JoinTeamUiEvent.ShowSnackbar("Welcome! You can sign in with your email and password next time."))
                    _uiEvent.send(JoinTeamUiEvent.NavigateToHome(result.data.user.role))
                }
                is AppResult.Failure ->
                    _uiEvent.send(JoinTeamUiEvent.ShowSnackbar(ErrorMessages.extractServerMessage(result.exception.message)))
                is AppResult.Loading -> Unit
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
