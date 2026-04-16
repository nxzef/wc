package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email) }
            }

            is LoginAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password) }
            }

            LoginAction.OnLoginClick -> login()
        }
    }

    private fun login() {
        val currentState = _state.value
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            loginUseCase(currentState.email, currentState.password)
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
