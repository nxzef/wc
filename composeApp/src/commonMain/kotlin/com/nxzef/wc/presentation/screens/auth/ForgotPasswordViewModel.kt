package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.remote.AuthService
import com.nxzef.wc.shared.model.ResetPasswordRequest
import com.nxzef.wc.shared.util.ErrorMessages
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val step: Int = 1 // 1: Email, 2: Reset
)

sealed interface ForgotPasswordUiEvent {
    data class ShowSnackbar(val message: String) : ForgotPasswordUiEvent
    data object NavigateToLogin : ForgotPasswordUiEvent
}

class ForgotPasswordViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ForgotPasswordUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onCodeChange(code: String) {
        _state.update { it.copy(code = code) }
    }

    fun onNewPasswordChange(password: String) {
        _state.update { it.copy(newPassword = password) }
    }

    fun onConfirmPasswordChange(password: String) {
        _state.update { it.copy(confirmPassword = password) }
    }

    fun sendCode() {
        if (state.value.email.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = authService.forgotPassword(state.value.email)
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    _state.update { it.copy(step = 2, isLoading = false) }
                    _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.extractServerMessage(body)))
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.extractServerMessage(body)))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.forGeneric(e.message)))
            }
        }
    }

    fun resetPassword() {
        val s = state.value
        if (s.code.isBlank() || s.newPassword.isBlank() || s.confirmPassword.isBlank()) return
        
        if (s.newPassword != s.confirmPassword) {
            viewModelScope.launch {
                _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar("Passwords do not match"))
            }
            return
        }

        if (s.newPassword.length < 6) {
            viewModelScope.launch {
                _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar("Password must be at least 6 characters"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = authService.resetPassword(
                    ResetPasswordRequest(
                        email = s.email,
                        code = s.code,
                        newPassword = s.newPassword
                    )
                )
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.extractServerMessage(body)))
                    _uiEvent.emit(ForgotPasswordUiEvent.NavigateToLogin)
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.extractServerMessage(body)))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(ForgotPasswordUiEvent.ShowSnackbar(ErrorMessages.forGeneric(e.message)))
            }
        }
    }
}
