package com.ejercicio.my_application_social.data.model

data class User(
    val id: Int = 0,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val bio: String? = null,
    val avatar_url: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)
data class AuthResponse(val token: String, val user_id: Int)