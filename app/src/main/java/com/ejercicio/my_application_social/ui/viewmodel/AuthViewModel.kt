package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.model.LoginRequest
import com.ejercicio.my_application_social.data.model.RegisterRequest
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

    val isLoggedIn = repository.currentAuthToken

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _state.value = AuthState.Error("Todos los campos son obligatorios")
            return
        }
        
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.login(LoginRequest(email.trim(), pass))
            result.fold(
                onSuccess = { _state.value = AuthState.Success },
                onFailure = { e -> _state.value = AuthState.Error(e.message ?: "Credenciales inválidas") }
            )
        }
    }

    fun register(name: String, user: String, email: String, pass: String) {
        if (name.isBlank() || user.isBlank() || email.isBlank() || pass.isBlank()) {
            _state.value = AuthState.Error("Todos los campos son obligatorios")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("Email inválido")
            return
        }
        
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.register(RegisterRequest(name.trim(), user.trim(), email.trim(), pass))
            result.fold(
                onSuccess = { _state.value = AuthState.Success },
                onFailure = { e -> _state.value = AuthState.Error(e.message ?: "Error en el registro") }
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