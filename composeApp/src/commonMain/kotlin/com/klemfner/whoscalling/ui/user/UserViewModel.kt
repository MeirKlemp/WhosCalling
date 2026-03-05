package com.klemfner.whoscalling.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klemfner.whoscalling.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
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
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(loginError = LoginError.BlankCredentials, isLoading = false) }
            } catch (_: Exception) {
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
