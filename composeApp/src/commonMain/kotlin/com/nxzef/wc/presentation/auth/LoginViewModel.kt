package com.nxzef.wc.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.model.UserRole
import com.nxzef.wc.data.remote.ApiService
import com.nxzef.wc.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    var email    = MutableStateFlow("")
    var password = MutableStateFlow("")

    fun onEmailChange(value: String) { email.value = value }
    fun onPasswordChange(value: String) { password.value = value }

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _state.value = LoginState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val response = apiService.login(
                    email.value.trim(),
                    password.value.trim()
                )
                SessionManager.save(response.token, response.user)
                _state.value = LoginState.Success(response.user.role)
            } catch (e: Exception) {
                _state.value = LoginState.Error(
                    "Login failed. Check credentials."
                )
            }
        }
    }
}