package com.klemfner.whoscalling.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.domain.repository.SettingsRepository
import com.klemfner.whoscalling.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "UserViewModel"
    }

    private val _uiState = MutableStateFlow(
        UserUiState(routerIp = settingsRepository.currentRouterIp),
    )
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.loggedInUser.collect { user ->
                _uiState.update {
                    it.copy(
                        loggedInUser = user,
                        isLoading = false,
                        password = if (user != null) "" else it.password,
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(routerIp = prefs.routerIp) }
            }
        }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateRememberMe(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun login() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                authRepository.login(state.username, state.password, state.rememberMe)
            } catch (_: IllegalArgumentException) {
                _uiState.update { it.copy(loginError = LoginError.BlankCredentials, isLoading = false) }
            } catch (e: Exception) {
                Logger.e(TAG, "Login failed", e)
                _uiState.update { it.copy(loginError = LoginError.Generic, isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(loginError = null) }
    }
}
