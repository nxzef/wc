package com.nxzef.wc.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.remote.ApiService
import com.nxzef.wc.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    val email    = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun onEmailChange(value: String)    { email.value = value }
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
                    e.message ?: "Login failed"
                )
            }
        }
    }
}