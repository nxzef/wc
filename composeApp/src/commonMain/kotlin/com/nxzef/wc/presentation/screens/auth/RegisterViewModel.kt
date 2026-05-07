package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.auth.RegisterUseCase
import com.nxzef.wc.shared.util.AppResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _uiEvent = Channel<RegisterUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnNameChange -> _state.update { it.copy(name = action.name) }
            is RegisterAction.OnTeamNameChange -> _state.update { it.copy(teamName = action.teamName) }
            is RegisterAction.OnEmailChange -> _state.update { it.copy(email = action.email) }
            is RegisterAction.OnPasswordChange -> _state.update { it.copy(password = action.password) }
            is RegisterAction.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = action.confirmPassword) }
            RegisterAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        val s = _state.value
        if (s.password != s.confirmPassword) {
            viewModelScope.launch {
                _uiEvent.send(RegisterUiEvent.ShowSnackbar("Passwords do not match"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = registerUseCase(s.name, s.email, s.password, s.teamName)) {
                is AppResult.Success ->
                    _uiEvent.send(RegisterUiEvent.NavigateToHome(result.data.user.role))
                is AppResult.Failure ->
                    _uiEvent.send(RegisterUiEvent.ShowSnackbar(result.exception.message ?: "Registration failed"))
                is AppResult.Loading -> Unit
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
