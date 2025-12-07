package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.model.LoginRequest
import com.ejercicio.my_application_social.data.model.RegisterRequest
import com.ejercicio.my_application_social.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val msg: String) : AuthState()
}

class AuthViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()
    
    val isLoggedIn = repository.token

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(email, pass))
                
                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    // CORRECCIÓN: Usamos 'token' en lugar de 'access_token'
                    repository.saveSession(auth.token, auth.user_id)
                    _state.value = AuthState.Success
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _state.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun register(name: String, user: String, email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = repository.register(RegisterRequest(name, user, email, pass))
                
                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    // CORRECCIÓN: Usamos 'token' en lugar de 'access_token'
                    repository.saveSession(auth.token, auth.user_id)
                    _state.value = AuthState.Success
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _state.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    private fun parseError(json: String?): String {
        return try {
            if (json == null) return "Error desconocido"
            val jsonObj = JSONObject(json)
            jsonObj.optString("message", "Error del servidor")
        } catch (e: Exception) {
            json ?: "Error desconocido"
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