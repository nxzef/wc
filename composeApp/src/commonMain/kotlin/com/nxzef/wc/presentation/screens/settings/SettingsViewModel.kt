package com.nxzef.wc.presentation.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.data.local.TokenStorage
import com.nxzef.wc.data.remote.ApiService
import com.nxzef.wc.data.session.SessionManager
import com.nxzef.wc.presentation.theme.AppTheme
import com.nxzef.wc.presentation.theme.ThemeManager
import com.nxzef.wc.shared.model.User
import com.nxzef.wc.shared.util.ErrorMessages
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    sessionManager: SessionManager
) : ViewModel() {

    val user: User? = sessionManager.getUser()

    private val _serverConnected = MutableStateFlow(false)
    val serverConnected = _serverConnected.asStateFlow()

    private val _appTheme = MutableStateFlow(ThemeManager.getTheme())
    val appTheme = _appTheme.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    var isChangingPassword by mutableStateOf(false)
        private set

    init {
        checkServerHealth()
    }

    fun checkServerHealth() {
        viewModelScope.launch {
            _serverConnected.value = apiService.checkHealth()
        }
    }

    fun setTheme(theme: AppTheme) {
        _appTheme.value = theme
        ThemeManager.setTheme(theme)
        viewModelScope.launch {
            tokenStorage.saveAppTheme(theme)
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            isChangingPassword = true
            try {
                apiService.changePassword(current, new)
                _message.emit("Password updated successfully")
            } catch (e: Exception) {
                _message.emit(ErrorMessages.forGeneric(e.message))
            } finally {
                isChangingPassword = false
            }
        }
    }
}
