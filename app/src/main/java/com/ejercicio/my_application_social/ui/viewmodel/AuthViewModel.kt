package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.model.LoginRequest // 👈 Importamos modelos de Request
import com.ejercicio.my_application_social.data.model.RegisterRequest // 👈 Importamos modelos de Request
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

    // Ahora devuelve el TOKEN (String?) de la sesión guardada
    val isLoggedIn = repository.currentAuthToken

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            // 🚨 CAMBIO 1: Creamos el objeto de petición
            val request = LoginRequest(email, pass)

            // 🚨 CAMBIO 2: Llamamos al repositorio que usa Retrofit
            val result = repository.login(request)

            result.fold(
                onSuccess = { authResponse ->
                    // El Repository ya guarda el token internamente.
                    // Solo confirmamos el éxito y navegamos.
                    _state.value = AuthState.Success
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "Credenciales inválidas o error de conexión")
                }
            )
        }
    }

    fun register(name: String, user: String, email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            // 🚨 CAMBIO 1: Creamos el objeto de petición
            val request = RegisterRequest(name, user, email, pass)

            // 🚨 CAMBIO 2: Llamamos al repositorio que usa Retrofit
            val result = repository.register(request)

            result.fold(
                onSuccess = { authResponse ->
                    // El Repository ya guarda el token internamente.
                    _state.value = AuthState.Success
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "El email ya está en uso o error de conexión")
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