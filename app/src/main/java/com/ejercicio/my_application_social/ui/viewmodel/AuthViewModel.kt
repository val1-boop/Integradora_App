package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val msg: String) : AuthState()
}

class AuthViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()
    
    // En el repositorio local, esto es un Flow<String?> que es el ID del usuario
    val isLoggedIn = repository.currentUserId

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    // Guardamos el ID del usuario como sesión
                    repository.saveSession(user.id)
                    _state.value = AuthState.Success
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }

    fun register(name: String, user: String, email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.register(name, user, email, pass)
            result.fold(
                onSuccess = { newUser ->
                    repository.saveSession(newUser.id)
                    _state.value = AuthState.Success
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _state.value = AuthState.Idle
        }
    }
    
    fun resetState() { _state.value = AuthState.Idle }
}