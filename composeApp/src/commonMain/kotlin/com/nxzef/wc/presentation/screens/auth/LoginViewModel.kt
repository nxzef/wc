package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                _state.update { it.copy(email = action.email) }
            }

            is LoginAction.PasswordChanged -> {
                _state.update { it.copy(password = action.password) }
            }

            LoginAction.Login -> login()
        }
    }

    private fun login() {
        val currentState = _state.value
        val email = currentState.email.trim()
        val password = currentState.password.trim()

        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(LoginUiEvent.ShowSnackbar("Please fill all fields"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.login(email, password)
                .onSuccess { response ->
                    _uiEvent.send(LoginUiEvent.NavigateToHome(response.user.role))
                }
                .onFailure { e ->
                    _uiEvent.send(LoginUiEvent.ShowSnackbar(e.message ?: "Login failed"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}